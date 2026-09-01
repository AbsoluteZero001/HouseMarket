package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.springboothousemarket.Entity.Appointment;
import com.springboot.springboothousemarket.Entity.AppointmentFlow;
import com.springboot.springboothousemarket.Entity.ChatMessage;
import com.springboot.springboothousemarket.Entity.Favorites;
import com.springboot.springboothousemarket.Entity.HouseImage;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Entity.NotificationOutbox;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Mapper.AppointmentFlowMapper;
import com.springboot.springboothousemarket.Mapper.AppointmentMapper;
import com.springboot.springboothousemarket.Mapper.ChatMessageMapper;
import com.springboot.springboothousemarket.Mapper.FavoritesMapper;
import com.springboot.springboothousemarket.Mapper.HousesMapper;
import com.springboot.springboothousemarket.Mapper.NotificationOutboxMapper;
import com.springboot.springboothousemarket.common.AppointmentStatus;
import com.springboot.springboothousemarket.common.HouseStatus;
import com.springboot.springboothousemarket.dto.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HousesServiceImpl extends ServiceImpl<HousesMapper, Houses> implements HousesService {

    private final HouseImageService houseImageService;
    private final ObjectMapper objectMapper;
    private final UsersService usersService;
    private final FavoritesMapper favoritesMapper;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentFlowMapper appointmentFlowMapper;
    private final NotificationOutboxMapper outboxMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final NotificationDispatcher notificationDispatcher;

    public HousesServiceImpl(HouseImageService houseImageService, ObjectMapper objectMapper, UsersService usersService,
                             FavoritesMapper favoritesMapper, AppointmentMapper appointmentMapper,
                             AppointmentFlowMapper appointmentFlowMapper, NotificationOutboxMapper outboxMapper,
                             ChatMessageMapper chatMessageMapper, NotificationDispatcher notificationDispatcher) {
        this.houseImageService = houseImageService;
        this.objectMapper = objectMapper;
        this.usersService = usersService;
        this.favoritesMapper = favoritesMapper;
        this.appointmentMapper = appointmentMapper;
        this.appointmentFlowMapper = appointmentFlowMapper;
        this.outboxMapper = outboxMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.notificationDispatcher = notificationDispatcher;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"houses:list", "home:stats"}, allEntries = true)
    public HouseDetailVO createHouse(HouseCreateDTO dto, Users currentUser) {
        if (currentUser == null) {
            throw new SecurityException("请先登录");
        }
        validateHousePayload(dto.getTitle(), dto.getPrice(), dto.getArea(), dto.getAddress(), dto.getLayout());

        Long landlordId = dto.getLandlordId();
        if (landlordId == null) {
            landlordId = currentUser.getId();
        }
        if (!"ADMIN".equals(currentUser.getRole()) && !landlordId.equals(currentUser.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("没有权限为其他房东创建房源");
        }

        Houses house = new Houses();
        copyToEntity(dto, house);
        house.setLandlordId(landlordId);
        // 状态机：房东发布进入待审核；管理员创建直接上架
        house.setStatus("ADMIN".equals(currentUser.getRole())
                ? normalizeStatus(dto.getStatus(), HouseStatus.NORMAL)
                : HouseStatus.PENDING_REVIEW);
        house.setViews(0);
        house.setIsDeleted(0);
        house.setImage("[]");
        house.setCreateTime(LocalDateTime.now());
        house.setUpdateTime(LocalDateTime.now());
        applyDefaults(house);
        save(house);

        createImagesFromUrls(house.getId(), dto.getImageUrls());
        return toDetailVO(house, houseImageService.listByHouseId(house.getId()));
    }

    @Override
    public HouseDetailVO getVisibleHouseDetailVO(Long id, Users viewer) {
        Houses house = getById(id);
        if (house == null || Integer.valueOf(1).equals(house.getIsDeleted())) {
            return null;
        }
        // 已下架/待审核/被拒绝的房源：仅房东本人与管理员可见
        if (!HouseStatus.NORMAL.equals(house.getStatus())) {
            boolean isOwner = viewer != null && house.getLandlordId().equals(viewer.getId());
            boolean isAdmin = viewer != null && "ADMIN".equals(viewer.getRole());
            if (!isOwner && !isAdmin) {
                return null;
            }
        }
        return toDetailVO(house, houseImageService.listByHouseId(id));
    }

    @Override
    public Houses getHouseById(Long id) {
        Houses house = getById(id);
        return house != null && Integer.valueOf(1).equals(house.getIsDeleted()) ? null : house;
    }

    @Override
    @CacheEvict(cacheNames = "houses:detail", key = "#id")
    public void incrementViews(Long id) {
        this.lambdaUpdate()
                .eq(Houses::getId, id)
                .setSql("views = views + 1")
                .update();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"houses:list", "home:stats", "houses:detail"}, key = "#id", allEntries = true)
    public HouseDetailVO updateHouse(Long id, HouseUpdateDTO dto, Users currentUser) {
        Houses dbHouse = getById(id);
        if (dbHouse == null || Integer.valueOf(1).equals(dbHouse.getIsDeleted())) {
            throw new RuntimeException("房源不存在");
        }
        assertCanManage(dbHouse, currentUser);
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());

        Houses house = dbHouse;
        copyToEntity(dto, house);
        house.setId(id);
        house.setLandlordId(resolveLandlordId(dbHouse, dto.getLandlordId(), currentUser));
        house.setStatus(dbHouse.getStatus());
        house.setViews(dbHouse.getViews());
        house.setIsDeleted(0);
        house.setImage("[]");
        house.setCreateTime(dbHouse.getCreateTime());
        house.setUpdateTime(LocalDateTime.now());
        updateById(house);

        if (dto.getImageUrls() != null) {
            houseImageService.deleteByHouseId(id);
            createImagesFromUrls(id, dto.getImageUrls());
        }

        // 状态机：非管理员编辑已上架/被拒绝的房源后，回到待审核重新审核
        if (!isAdmin && (HouseStatus.NORMAL.equals(dbHouse.getStatus())
                || HouseStatus.REJECTED.equals(dbHouse.getStatus()))) {
            lambdaUpdate()
                    .eq(Houses::getId, id)
                    .set(Houses::getStatus, HouseStatus.PENDING_REVIEW)
                    .set(Houses::getReviewNote, null)
                    .update();
            house.setStatus(HouseStatus.PENDING_REVIEW);
        }
        return toDetailVO(house, houseImageService.listByHouseId(id));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"houses:list", "home:stats", "houses:detail"}, key = "#id", allEntries = true)
    public boolean deleteHouse(Long id, Users currentUser) {
        Houses dbHouse = getById(id);
        if (dbHouse == null || Integer.valueOf(1).equals(dbHouse.getIsDeleted())) {
            throw new RuntimeException("房源不存在");
        }
        assertCanManage(dbHouse, currentUser);

        // 存在进行中的预约时禁止删除，避免租客预约凭空消失
        long activeAppointments = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getHouseId, id)
                        .in(Appointment::getStatus, AppointmentStatus.OCCUPYING));
        if (activeAppointments > 0) {
            throw new RuntimeException("该房源存在待处理的看房预约，请先处理预约后再删除");
        }

        // 级联清理：图片(含文件)、收藏、已终结预约(轨迹/Outbox/记录)、聊天房源引用
        houseImageService.deleteByHouseId(id);
        favoritesMapper.delete(new LambdaQueryWrapper<Favorites>().eq(Favorites::getHouseId, id));
        List<Appointment> terminalAppointments = appointmentMapper.selectList(
                new LambdaQueryWrapper<Appointment>().eq(Appointment::getHouseId, id));
        for (Appointment appointment : terminalAppointments) {
            outboxMapper.delete(new LambdaQueryWrapper<NotificationOutbox>()
                    .eq(NotificationOutbox::getAppointmentId, appointment.getId()));
            appointmentFlowMapper.delete(new LambdaQueryWrapper<AppointmentFlow>()
                    .eq(AppointmentFlow::getAppointmentId, appointment.getId()));
            appointmentMapper.deleteById(appointment.getId());
        }
        chatMessageMapper.update(null,
                new LambdaUpdateWrapper<ChatMessage>()
                        .eq(ChatMessage::getHouseId, id)
                        .set(ChatMessage::getHouseId, null));

        dbHouse.setIsDeleted(1);
        dbHouse.setUpdateTime(LocalDateTime.now());
        return updateById(dbHouse);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"houses:list", "home:stats", "houses:detail"}, key = "#id", allEntries = true)
    public Houses changeStatus(Long id, String targetStatus, String note, Users currentUser) {
        Houses house = getById(id);
        if (house == null || Integer.valueOf(1).equals(house.getIsDeleted())) {
            throw new RuntimeException("房源不存在");
        }
        if (!HouseStatus.isValid(targetStatus)) {
            throw new RuntimeException("非法的房源状态: " + targetStatus);
        }
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());
        if (!isAdmin) {
            if (!house.getLandlordId().equals(currentUser.getId())) {
                throw new org.springframework.security.access.AccessDeniedException("没有权限操作此房源");
            }
            if (!HouseStatus.canLandlordSwitch(house.getStatus(), targetStatus)) {
                throw new RuntimeException("当前状态不允许该操作（仅支持 上架↔下架）");
            }
        }
        house.setStatus(targetStatus);
        if (note != null) {
            house.setReviewNote(note);
        }
        house.setUpdateTime(LocalDateTime.now());
        updateById(house);
        return house;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"houses:list", "home:stats", "houses:detail"}, allEntries = true)
    public HouseDetailVO reviewHouse(Long id, boolean approve, String note, Users admin) {
        Houses house = getById(id);
        if (house == null || Integer.valueOf(1).equals(house.getIsDeleted())) {
            throw new RuntimeException("房源不存在");
        }
        if (!HouseStatus.PENDING_REVIEW.equals(house.getStatus())) {
            throw new RuntimeException("仅待审核状态的房源可以审核");
        }
        if (!approve && (note == null || note.isBlank())) {
            throw new RuntimeException("拒绝审核时必须填写审核意见");
        }
        house.setStatus(approve ? HouseStatus.NORMAL : HouseStatus.REJECTED);
        house.setReviewNote(approve ? (note == null || note.isBlank() ? "审核通过" : note) : note);
        house.setUpdateTime(LocalDateTime.now());
        updateById(house);

        // 通知房东审核结果
        notificationDispatcher.dispatch(
                "HOUSE:" + id + (approve ? ":HOUSE_APPROVED" : ":HOUSE_REJECTED"),
                "HOUSE",
                null,
                house.getLandlordId(),
                approve ? "HOUSE_APPROVED" : "HOUSE_REJECTED",
                approve ? "房源审核通过" : "房源审核未通过",
                approve
                        ? "您发布的「" + house.getTitle() + "」已通过审核并正式上架"
                        : "您发布的「" + house.getTitle() + "」未通过审核：" + house.getReviewNote() + "，修改后可重新提交",
                "HOUSE", id);
        return toDetailVO(house, houseImageService.listByHouseId(id));
    }

    @Override
    public List<HouseListVO> getHousesByLandlordVO(Long landlordId) {
        List<Houses> houses = list(new QueryWrapper<Houses>()
                .eq("landlord_id", landlordId)
                .eq("is_deleted", 0)
                .orderByDesc("create_time"));
        return toListVO(houses);
    }

    @Override
    public Page<HouseListVO> getHouseListVO(String keyword, String type, String district, Double minArea, Double maxArea,
                                            Double minPrice, Double maxPrice, String address, String status, int page, int pageSize) {
        QueryWrapper<Houses> query = new QueryWrapper<>();
        query.eq("is_deleted", 0);
        if (status != null && !status.isBlank()) {
            query.eq("status", status);
        }
        if (keyword != null && !keyword.isBlank()) {
            query.and(wrapper -> wrapper
                    .like("title", keyword)
                    .or()
                    .like("address", keyword)
                    .or()
                    .like("district", keyword));
        }
        if (type != null && !type.isBlank()) {
            query.eq("type", type);
        }
        if (district != null && !district.isBlank()) {
            query.like("district", district);
        }
        if (minArea != null) {
            query.ge("area", minArea);
        }
        if (maxArea != null) {
            query.le("area", maxArea);
        }
        if (minPrice != null) {
            query.ge("price", minPrice);
        }
        if (maxPrice != null) {
            query.le("price", maxPrice);
        }
        if (address != null && !address.isBlank()) {
            query.like("address", address);
        }
        query.orderByDesc("create_time");

        Page<Houses> entityPage = page(new Page<>(page, pageSize), query);
        Page<HouseListVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(toListVO(entityPage.getRecords()));
        return voPage;
    }

    private void validateHousePayload(String title, BigDecimal price, BigDecimal area, String address, String layout) {
        if (title == null || title.isBlank()) {
            throw new BusinessException("房源标题不能为空");
        }
        if (title.length() > 100) {
            throw new BusinessException("房源标题不能超过100个字符");
        }
        if (price == null || price.signum() <= 0) {
            throw new BusinessException("租金必须大于0");
        }
        if (area == null || area.signum() <= 0) {
            throw new BusinessException("面积必须大于0");
        }
        if (address == null || address.isBlank()) {
            throw new BusinessException("详细地址不能为空");
        }
        if (layout == null || layout.isBlank()) {
            throw new BusinessException("请选择户型");
        }
    }

    private List<HouseListVO> toListVO(List<Houses> houses) {
        if (houses == null || houses.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, List<HouseImage>> imageMap = houseImageService.listByHouseIds(
                houses.stream().map(Houses::getId).filter(Objects::nonNull).collect(Collectors.toSet()));
        return houses.stream()
                .map(house -> toListVO(house, imageMap.getOrDefault(house.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    private HouseListVO toListVO(Houses house, List<HouseImage> images) {
        HouseListVO vo = new HouseListVO();
        copyBase(house, vo);
        vo.setCoverImage(findCover(images));
        return vo;
    }

    private LandlordInfoVO buildLandlordInfo(Long landlordId) {
        if (landlordId == null) {
            return null;
        }
        Users landlord = usersService.getUserById(landlordId);
        if (landlord == null) {
            return null;
        }
        LandlordInfoVO vo = new LandlordInfoVO();
        vo.setId(landlord.getId());
        vo.setUsername(landlord.getUsername());
        vo.setNickname(landlord.getNickname());
        vo.setRealName(landlord.getRealName());
        vo.setRealNameVerified(landlord.getRealNameVerified());
        vo.setVerifiedTime(landlord.getVerifiedTime());
        vo.setIdCardNoMasked(maskIdCard(landlord.getIdCardNo()));
        if (Integer.valueOf(1).equals(landlord.getRealNameVerified())) {
            vo.setPhone(landlord.getPhone());
        }
        return vo;
    }

    private String maskIdCard(String idCardNo) {
        if (idCardNo == null || idCardNo.length() < 15) {
            return null;
        }
        return idCardNo.substring(0, 6) + "********" + idCardNo.substring(14);
    }

    private HouseDetailVO toDetailVO(Houses house, List<HouseImage> images) {
        HouseDetailVO vo = new HouseDetailVO();
        copyBase(house, vo);
        vo.setDescription(house.getDescription());
        vo.setImages(images.stream()
                .sorted(Comparator.comparing(HouseImage::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(HouseImage::getId))
                .map(this::toImageVO)
                .collect(Collectors.toList()));
        vo.setCoverImage(findCover(images));
        vo.setLandlord(buildLandlordInfo(house.getLandlordId()));
        return vo;
    }

    private void copyBase(Houses house, HouseListVO vo) {
        vo.setId(house.getId());
        vo.setTitle(house.getTitle());
        vo.setType(house.getType());
        vo.setLayout(house.getLayout());
        vo.setDistrict(house.getDistrict());
        vo.setCommunity(house.getCommunity());
        vo.setBedrooms(house.getBedrooms());
        vo.setLivingRooms(house.getLivingRooms());
        vo.setKitchens(house.getKitchens());
        vo.setBathrooms(house.getBathrooms());
        vo.setArea(house.getArea());
        vo.setPrice(house.getPrice());
        vo.setDeposit(house.getDeposit());
        vo.setOrientation(house.getOrientation());
        vo.setFloor(house.getFloor());
        vo.setTotalFloors(house.getTotalFloors());
        vo.setDecoration(house.getDecoration());
        vo.setLeaseTerm(house.getLeaseTerm());
        vo.setHasElevator(house.getHasElevator());
        vo.setSubwayDistance(house.getSubwayDistance());
        vo.setMoveInType(house.getMoveInType());
        vo.setRentStatus(house.getRentStatus());
        vo.setTags(parseStringList(house.getTags()));
        vo.setAddress(house.getAddress());
        vo.setLandlordId(house.getLandlordId());
        vo.setStatus(house.getStatus());
        vo.setReviewNote(house.getReviewNote());
        vo.setViews(house.getViews());
        vo.setCreateTime(house.getCreateTime());
        vo.setUpdateTime(house.getUpdateTime());
    }

    private HouseImageVO toImageVO(HouseImage image) {
        HouseImageVO vo = new HouseImageVO();
        vo.setId(image.getId());
        vo.setHouseId(image.getHouseId());
        vo.setImageUrl(image.getImageUrl());
        vo.setImageType(image.getImageType());
        vo.setSortOrder(image.getSortOrder());
        vo.setIsCover(image.getIsCover());
        vo.setCreateTime(image.getCreateTime());
        return vo;
    }

    private String findCover(List<HouseImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
                .filter(image -> Integer.valueOf(1).equals(image.getIsCover()))
                .min(Comparator.comparing(HouseImage::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(HouseImage::getId))
                .orElse(images.get(0))
                .getImageUrl();
    }

    private List<String> parseStringList(String value) {
        if (value == null || value.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private void copyToEntity(HouseCreateDTO dto, Houses house) {
        house.setTitle(dto.getTitle());
        house.setType(dto.getType());
        house.setLayout(dto.getLayout());
        house.setDistrict(dto.getDistrict());
        house.setCommunity(dto.getCommunity());
        house.setBedrooms(dto.getBedrooms());
        house.setLivingRooms(dto.getLivingRooms());
        house.setKitchens(dto.getKitchens());
        house.setBathrooms(dto.getBathrooms());
        house.setArea(dto.getArea());
        house.setPrice(dto.getPrice());
        house.setDeposit(dto.getDeposit());
        house.setOrientation(dto.getOrientation());
        house.setFloor(dto.getFloor());
        house.setTotalFloors(dto.getTotalFloors());
        house.setDecoration(dto.getDecoration());
        house.setLeaseTerm(dto.getLeaseTerm());
        house.setHasElevator(dto.getHasElevator());
        house.setSubwayDistance(dto.getSubwayDistance());
        house.setMoveInType(dto.getMoveInType());
        house.setRentStatus(dto.getRentStatus());
        house.setTags(normalizeTags(dto.getTags()));
        house.setAddress(dto.getAddress());
        house.setDescription(dto.getDescription());
    }

    private void copyToEntity(HouseUpdateDTO dto, Houses house) {
        if (dto.getTitle() != null) house.setTitle(dto.getTitle());
        if (dto.getType() != null) house.setType(dto.getType());
        if (dto.getLayout() != null) house.setLayout(dto.getLayout());
        if (dto.getDistrict() != null) house.setDistrict(dto.getDistrict());
        if (dto.getCommunity() != null) house.setCommunity(dto.getCommunity());
        if (dto.getBedrooms() != null) house.setBedrooms(dto.getBedrooms());
        if (dto.getLivingRooms() != null) house.setLivingRooms(dto.getLivingRooms());
        if (dto.getKitchens() != null) house.setKitchens(dto.getKitchens());
        if (dto.getBathrooms() != null) house.setBathrooms(dto.getBathrooms());
        if (dto.getArea() != null) house.setArea(dto.getArea());
        if (dto.getPrice() != null) house.setPrice(dto.getPrice());
        if (dto.getDeposit() != null) house.setDeposit(dto.getDeposit());
        if (dto.getOrientation() != null) house.setOrientation(dto.getOrientation());
        if (dto.getFloor() != null) house.setFloor(dto.getFloor());
        if (dto.getTotalFloors() != null) house.setTotalFloors(dto.getTotalFloors());
        if (dto.getDecoration() != null) house.setDecoration(dto.getDecoration());
        if (dto.getLeaseTerm() != null) house.setLeaseTerm(dto.getLeaseTerm());
        if (dto.getHasElevator() != null) house.setHasElevator(dto.getHasElevator());
        if (dto.getSubwayDistance() != null) house.setSubwayDistance(dto.getSubwayDistance());
        if (dto.getMoveInType() != null) house.setMoveInType(dto.getMoveInType());
        if (dto.getRentStatus() != null) house.setRentStatus(dto.getRentStatus());
        if (dto.getTags() != null) house.setTags(normalizeTags(dto.getTags()));
        if (dto.getAddress() != null) house.setAddress(dto.getAddress());
        if (dto.getDescription() != null) house.setDescription(dto.getDescription());
    }

    private String normalizeTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return "[]";
        }
        String trimmed = tags.trim();
        if (trimmed.startsWith("[")) {
            return trimmed;
        }
        List<String> values = List.of(trimmed.split(","));
        try {
            return objectMapper.writeValueAsString(values.stream()
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .collect(Collectors.toList()));
        } catch (Exception ignored) {
            return "[]";
        }
    }

    private void applyDefaults(Houses house) {
        if (house.getTitle() == null || house.getTitle().isBlank()) {
            house.setTitle("未命名房源");
        }
        if (house.getType() == null || house.getType().isBlank()) {
            house.setType("平层");
        }
        if (house.getLayout() == null || house.getLayout().isBlank()) {
            house.setLayout("其他");
        }
        if (house.getDistrict() == null || house.getDistrict().isBlank()) {
            house.setDistrict("未知区域");
        }
        if (house.getBedrooms() == null) {
            house.setBedrooms(1);
        }
        if (house.getBathrooms() == null) {
            house.setBathrooms(1);
        }
        if (house.getLivingRooms() == null) {
            house.setLivingRooms(0);
        }
        if (house.getKitchens() == null) {
            house.setKitchens(0);
        }
        if (house.getArea() == null) {
            house.setArea(BigDecimal.ZERO);
        }
        if (house.getPrice() == null) {
            house.setPrice(BigDecimal.ZERO);
        }
        if (house.getDeposit() == null) {
            house.setDeposit(BigDecimal.ZERO);
        }
        if (house.getOrientation() == null || house.getOrientation().isBlank()) {
            house.setOrientation("南北");
        }
        if (house.getDecoration() == null || house.getDecoration().isBlank()) {
            house.setDecoration("精装");
        }
        if (house.getLeaseTerm() == null || house.getLeaseTerm().isBlank()) {
            house.setLeaseTerm("押一付三");
        }
        if (house.getHasElevator() == null) {
            house.setHasElevator(0);
        }
        if (house.getMoveInType() == null || house.getMoveInType().isBlank()) {
            house.setMoveInType("随时入住");
        }
        if (house.getRentStatus() == null || house.getRentStatus().isBlank()) {
            house.setRentStatus("随时入住");
        }
        if (house.getTags() == null || house.getTags().isBlank()) {
            house.setTags("[]");
        }
        if (house.getAddress() == null || house.getAddress().isBlank()) {
            house.setAddress("未知地址");
        }
        if (house.getDescription() == null || house.getDescription().isBlank()) {
            house.setDescription("暂无描述");
        }
        if (house.getImage() == null || house.getImage().isBlank()) {
            house.setImage("[]");
        }
    }

    private void createImagesFromUrls(Long houseId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        for (int i = 0; i < imageUrls.size(); i++) {
            String url = imageUrls.get(i);
            if (url != null && !url.isBlank()) {
                houseImageService.createImage(houseId, url, "OTHER", i, i == 0);
            }
        }
    }

    private String normalizeStatus(String status, String defaultStatus) {
        if (status == null || status.isBlank()) {
            return defaultStatus;
        }
        if (!HouseStatus.isValid(status)) {
            throw new BusinessException("非法的房源状态: " + status + "，允许值: PENDING_REVIEW/NORMAL/OFFLINE/REJECTED");
        }
        return status;
    }

    private Long resolveLandlordId(Houses dbHouse, Long requestedLandlordId, Users currentUser) {
        if (requestedLandlordId != null) {
            if (!"ADMIN".equals(currentUser.getRole()) && !requestedLandlordId.equals(currentUser.getId())) {
                throw new org.springframework.security.access.AccessDeniedException("没有权限变更房源归属");
            }
            return requestedLandlordId;
        }
        return "ADMIN".equals(currentUser.getRole()) ? dbHouse.getLandlordId() : currentUser.getId();
    }

    private void assertCanManage(Houses house, Users currentUser) {
        if (currentUser == null) {
            throw new SecurityException("请先登录");
        }
        if (!"ADMIN".equals(currentUser.getRole()) && !house.getLandlordId().equals(currentUser.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("没有权限操作此房源");
        }
    }
}
