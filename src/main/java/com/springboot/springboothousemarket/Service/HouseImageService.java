package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.HouseImage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface HouseImageService extends IService<HouseImage> {

    List<HouseImage> listByHouseId(Long houseId);

    Map<Long, List<HouseImage>> listByHouseIds(Collection<Long> houseIds);

    HouseImage uploadImage(Long houseId, MultipartFile file, String imageType, Integer sortOrder, boolean cover) throws IOException;

    HouseImage createImage(Long houseId, String imageUrl, String imageType, Integer sortOrder, boolean cover);

    void deleteImage(Long houseId, Long imageId);

    HouseImage setCover(Long houseId, Long imageId);

    void reorder(Long houseId, List<Long> imageIds);

    void deleteByHouseId(Long houseId);
}
