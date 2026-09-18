# -*- coding: utf-8 -*-
"""房源市场 端到端业务闭环测试"""
import json, time, urllib.request, urllib.error, sys, datetime

BASE = 'http://localhost:8082'
PASS, FAIL = [], []

def call(method, path, token=None, body=None, expect_status=200):
    url = BASE + path
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    if body is not None: req.add_header('Content-Type', 'application/json')
    if token: req.add_header('Authorization', 'Bearer ' + token)
    try:
        with urllib.request.urlopen(req) as r:
            return r.status, json.loads(r.read().decode())
    except urllib.error.HTTPError as e:
        try: return e.code, json.loads(e.read().decode())
        except Exception: return e.code, {}

def check(name, cond, detail=''):
    if cond: PASS.append(name); print(f"  [PASS] {name}")
    else: FAIL.append(name); print(f"  [FAIL] {name}  {detail}")

def login(username, password, role='TENANT'):
    s, r = call('POST', '/api/v1/auth/login', body={'username': username, 'password': password, 'role': role})
    return s, r

print("=" * 60)
print("一、用户链：注册 → 登录 → 改昵称 → 改密码 → 重新登录")
print("=" * 60)
ts = str(int(time.time()))
t_user, t_pwd = 'e2e_tenant_' + ts[-6:], 'pass123'
s, r = call('POST', '/api/v1/auth/register', body={'username': t_user, 'password': t_pwd, 'nickname': 'E2E租客', 'role': 'TENANT'})
check('注册 TENANT 成功', s == 200 and r.get('success'), str(r))
s, r = login(t_user, t_pwd, 'TENANT')
check('登录成功', s == 200 and r.get('code') == 200, str(r))
tenant_token = r.get('token'); tenant_id = r['data']['id']
check('登录返回 TENANT 角色', r['data']['role'] == 'TENANT', str(r.get('data', {}).get('role')))

# 改昵称
s, r = call('PUT', '/user/profile', tenant_token, {'nickname': 'E2E改名租客'})
check('修改昵称成功', s == 200 and r['data']['user']['nickname'] == 'E2E改名租客', str(r))

# 改密码：旧密码错误应被拒
s, r = call('PUT', f"/user/{tenant_id}/password", tenant_token, {'oldPassword': 'wrong!', 'newPassword': 'newpass456'})
check('旧密码错误时改密码被拒(400)', s == 400, f"status={s} {r}")
# 改密码：旧密码为空被拒
s, r = call('PUT', f"/user/{tenant_id}/password", tenant_token, {'oldPassword': '', 'newPassword': 'newpass456'})
check('旧密码为空被拒(400)', s == 400, f"status={s}")
# 弱密码被拒
s, r = call('PUT', f"/user/{tenant_id}/password", tenant_token, {'oldPassword': t_pwd, 'newPassword': '123'})
check('弱密码被拒(400)', s == 400, f"status={s}")
# 正确修改
s, r = call('PUT', f"/user/{tenant_id}/password", tenant_token, {'oldPassword': t_pwd, 'newPassword': 'newpass456'})
check('修改密码成功', s == 200 and r.get('success'), f"status={s} {r}")
# 旧密码不能再登录 / 新密码可登录
s, r = login(t_user, t_pwd, 'TENANT')
check('旧密码登录失败', r.get('code') != 200, str(r.get('msg')))
s, r = login(t_user, 'newpass456', 'TENANT')
check('新密码登录成功', s == 200 and r.get('code') == 200, str(r))
tenant_token = r['token']

print()
print("=" * 60)
print("二、房东入驻链：TENANT 申请 → ADMIN 审核 → 角色升级")
print("=" * 60)
s, r = call('POST', '/api/landlord/application', tenant_token, {'realName': '王测试', 'phone': '13900009999'})
check('TENANT 提交房东入驻申请', s == 200 and r.get('success'), f"status={s} {r}")
app_id = r['data']['application']['id']

# 未审核时不能发布房源
s, r = call('POST', '/api/houses/add', tenant_token, {'title': 'x', 'price': 100, 'area': 10, 'address': 'y', 'layout': '一室一厅'})
check('未通过审核发布房源被拒', s in (400, 403), f"status={s} {r}")

# admin 登录
s, r = login('admin', 'admin123', 'ADMIN')
admin_token = r['token']
check('管理员登录', s == 200 and r.get('code') == 200, str(r))

# 管理员拒绝 → 租客重新提交 → 管理员通过
s, r = call('PUT', f'/api/admin/landlord-applications/{app_id}/reject', admin_token, {'note': '资料不全'})
check('管理员拒绝申请', s == 200 and r.get('success'), f"status={s} {r}")
s, r = call('POST', '/api/landlord/application', tenant_token, {'realName': '王测试', 'phone': '13900009998'})
check('被拒后重新提交申请', s == 200 and r['data']['application']['status'] == 'pending', f"status={s} {r}")
app_id = r['data']['application']['id']
s, r = call('PUT', f'/api/admin/landlord-applications/{app_id}/approve', admin_token, {'note': 'ok'})
check('管理员通过申请', s == 200 and r.get('success'), f"status={s} {r}")

# 角色必须已升级为 LANDLORD（重新登录验证）
s, r = login(t_user, 'newpass456', 'LANDLORD')
check('审核通过后角色升级为 LANDLORD', s == 200 and r.get('code') == 200 and r['data']['role'] == 'LANDLORD', str(r.get('data', {}).get('role')))
landlord_token = r['token']
e2e_landlord_id = r['data']['id']

# 通知：审核通过的通知应存在
s, r = call('GET', '/api/notifications', tenant_token)
types = [n['type'] for n in r['data']['notifications']]
check('租客收到审核结果通知(含拒绝+通过)', 'LANDLORD_APPROVED' in types and 'LANDLORD_REJECTED' in types, str(types))
unread_before = r['data']['unread']
nid = r['data']['notifications'][0]['id']
s, r = call('PUT', f'/api/notifications/{nid}/read', tenant_token)
check('标记单条已读', s == 200, str(r))
s, r = call('GET', '/api/notifications/unread-count', tenant_token)
check('未读数减少', r['data']['unread'] == unread_before - 1, f"{unread_before}->{r['data']['unread']}")
s, r = call('PUT', '/api/notifications/read-all', tenant_token)
check('全部已读', s == 200, str(r))

print()
print("=" * 60)
print("三、实名认证链：提交 → 管理员审核 → 生效")
print("=" * 60)
e2e_card = '11010119900101' + ts[-4:].zfill(4)
s, r = call('POST', '/api/identity-verification', landlord_token, {'realName': '王测试', 'idCardNo': e2e_card})
check('提交实名认证申请', s == 200 and r.get('success'), f"status={s} {r}")
vid = r['data']['verification']['id']
check('返回脱敏身份证号', '***' in r['data']['verification']['idCardNoMasked'], str(r['data']['verification'].get('idCardNoMasked')))
# 重复身份证号校验：landlord1 的身份证 110101199001011234 已被使用
s, r = call('POST', '/api/identity-verification', landlord_token, {'realName': '王测试', 'idCardNo': '110101199001011234'})
check('他人已用身份证号被拒', s == 400, f"status={s} {r}")
s, r = call('PUT', f'/api/admin/identity-verifications/{vid}/approve', admin_token, {'note': 'ok'})
check('管理员通过实名认证', s == 200 and r.get('success'), f"status={s} {r}")
s, r = call('GET', '/api/landlord/profile', landlord_token)
check('实名状态已生效 realNameVerified=1', r['data']['user']['realNameVerified'] == 1, str(r['data']['user'].get('realNameVerified')))

print()
print("=" * 60)
print("四、房东发布房源 → 管理员审核房源 → 上架")
print("=" * 60)
house_payload = {'title': 'E2E测试房源 高铁站旁精装两居', 'type': '平层', 'layout': '两室一厅', 'district': '朝阳区',
                 'bedrooms': 2, 'bathrooms': 1, 'area': 88.5, 'price': 6000, 'deposit': 6000,
                 'address': '朝阳区E2E测试路1号', 'description': 'E2E测试房源描述', 'tags': '[]'}
s, r = call('POST', '/api/houses/add', landlord_token, house_payload)
check('房东发布房源(进入待审核)', s == 200 and r['data']['house']['status'] == 'PENDING_REVIEW', f"status={s} {r}")
house_id = r['data']['house']['id']
# 校验：非法状态值
bad = dict(house_payload); bad['status'] = 'HACKED'
s, r = call('POST', '/api/houses/add', landlord_token, bad)
check('房东传入非法状态值被忽略(仍为PENDING_REVIEW)', r['data']['house']['status']=='PENDING_REVIEW', str(r.get('data',{}).get('house',{}).get('status')))
# 待审核房源公开列表不可见
s, r = call('GET', '/api/houses?page=1&pageSize=100')
check('待审核房源不在公开列表', all(h['id'] != house_id for h in r['data']['houses']), '')
s, r = call('GET', f'/api/houses/{house_id}')
check('待审核房源匿名详情不可见(400)', s == 400, f"status={s} {r}")
# 匿名不能预约待审核房源
future = (datetime.datetime.now() + datetime.timedelta(days=1)).strftime('%Y-%m-%d %H:%M')
s, r = call('POST', '/api/appointments', tenant_token, {'houseId': house_id, 'time': future, 'location': '楼下'})
check('待审核房源不能被预约', s == 400, f"status={s} {r}")
# 管理员审核通过
s, r = call('PUT', f'/api/houses/{house_id}/review', admin_token, {'approve': True, 'note': 'ok'})
check('管理员审核房源通过→NORMAL', s == 200 and r['data']['house']['status'] == 'NORMAL', f"status={s} {r}")
s, r = call('GET', f'/api/houses/{house_id}')
check('上架后匿名可见', s == 200 and r['data']['house']['id'] == house_id, f"status={s}")
# 房东收到房源审核通过通知
s, r = call('GET', '/api/notifications', landlord_token)
types = [n['type'] for n in r['data']['notifications']]
check('房东收到房源审核通知', 'HOUSE_APPROVED' in types, str(types))

print()
print("=" * 60)
print("五、租客找房链：浏览→收藏→预约")
print("=" * 60)
# 独立租客账号（房东链账号已升级为 LANDLORD，不再兼具租客身份）
r_user = 'e2e_renter_' + ts[-6:]
s, r = call('POST', '/api/v1/auth/register', body={'username': r_user, 'password': 'pass123', 'nickname': 'E2E找房租客', 'role': 'TENANT'})
s, r = login(r_user, 'pass123', 'TENANT')
renter_token = r['token']
renter_id = r['data']['id']
check('注册并登录找房租客', s == 200 and r['data']['role'] == 'TENANT', str(r))
# 管理员不能把房源状态改成非法值（业务层校验，非 DB 500）
s, r = call('PUT', f'/api/houses/{house_id}/status', admin_token, {'status': 'HACKED'})
check('管理员设置非法房源状态被拒(400)', s == 400, f"status={s} {r}")

s, r = call('GET', '/api/houses?page=1&pageSize=100')
check('公开列表只含 NORMAL 房源', all(h['status'] == 'NORMAL' for h in r['data']['houses']), str({h['status'] for h in r['data']['houses']}))
s, r = call('GET', '/api/favorites', renter_token)
check('收藏列表为空起点', len(r['data']['favorites']) == 0, str(r))
s, r = call('POST', '/api/favorites', renter_token, {'houseId': house_id})
check('收藏房源', s == 200, f"status={s} {r}")
s, r = call('POST', '/api/favorites', renter_token, {'houseId': house_id})
check('重复收藏幂等成功', s == 200, f"status={s} {r}")
s, r = call('GET', '/api/favorites', renter_token)
check('收藏列表有1条(无重复)', len([f for f in r['data']['favorites'] if f['houseId'] == house_id]) == 1, str(r))
s, r = call('GET', '/api/favorites/check?houseId=' + str(house_id), renter_token)
check('收藏状态检查为 true', r['data']['favorited'] == True, str(r))
# 收藏不存在的房源
s, r = call('POST', '/api/favorites', tenant_token, {'houseId': 99999})
check('收藏不存在房源被业务层拒绝(400)', s == 400, f"status={s} {r}")

print()
print("=" * 60)
print("六、预约链 + 异常场景")
print("=" * 60)
s, r = call('POST', '/api/appointments', renter_token, {'houseId': house_id, 'time': '2020-01-01 10:00', 'location': '楼下'})
check('过去时间预约被拒', s == 400, f"status={s} {r}")
future = (datetime.datetime.now() + datetime.timedelta(days=2)).replace(hour=15, minute=0, second=0, microsecond=0).strftime('%Y-%m-%d %H:%M')
s, r = call('POST', '/api/appointments', renter_token, {'houseId': house_id, 'time': future, 'location': 'E2E测试路1号', 'requestId': 'e2e-req-' + ts[-6:]})
check('创建预约成功', s == 200 and r.get('success'), f"status={s} {r}")
apt_id = r['data']['id']
s, r = call('POST', '/api/appointments', renter_token, {'houseId': house_id, 'time': future, 'location': 'E2E测试路1号', 'requestId': 'e2e-req-' + ts[-6:]})
check('相同 requestId 幂等返回同一预约', r['data']['id'] == apt_id, str(r))
# 同一房源同一时段第二个租客被拒（tenant2 种子账号，密码 123456）
s, r = login('tenant2', '123456', 'TENANT')
t2_token = r['token']
s, r = call('POST', '/api/appointments', t2_token, {'houseId': house_id, 'time': future, 'location': 'E2E测试路1号'})
check('同时段重复预约被拒(冲突检测)', s == 400, f"status={s} {r}")
s, r = call('GET', '/api/notifications', landlord_token)
types = [n['type'] for n in r['data']['notifications']]
check('房东收到新预约通知', 'APPOINTMENT_CREATED' in types, str(types))
s, r = login('landlord2', '123456', 'LANDLORD')
l2_token = r['token']
s, r = call('PUT', f'/api/appointments/{apt_id}/approve', l2_token)
check('非所属房东审批被拒(403)', s == 403, f"status={s} {r}")
s, r = call('PUT', f'/api/appointments/{apt_id}/approve', renter_token)
check('租客审批被拒(403)', s == 403, f"status={s}")
s, r = call('PUT', f'/api/appointments/{apt_id}/approve', landlord_token)
check('房东批准预约', s == 200 and r.get('success'), f"status={s} {r}")
s, r = call('PUT', f'/api/appointments/{apt_id}/approve', landlord_token)
check('重复批准被拒(状态机校验)', s == 400, f"status={s} {r}")
s, r = call('GET', '/api/notifications', renter_token)
types = [n['type'] for n in r['data']['notifications']]
check('租客收到批准通知', 'APPOINTMENT_APPROVED' in types, str(types))
s, r = call('PUT', f'/api/appointments/{apt_id}/complete', landlord_token)
check('房东标记完成', s == 200, f"status={s} {r}")
s, r = call('GET', f'/api/appointments/{apt_id}/flow', landlord_token)
actions = [f['action'] for f in r['data']['flows']]
check('预约轨迹完整(PUBLISH/BOOK/APPROVE/COMPLETE/NOTIFY)', all(a in actions for a in ['PUBLISH','BOOK','APPROVE','COMPLETE','NOTIFY']), str(actions))

print()
print("=" * 60)
print("七、管理员链：用户管理 + 禁用 + 自我保护 + 权限")
print("=" * 60)
s, r = call('GET', '/user', admin_token)
users_list = r['data']['users']
check('用户列表返回(无密码字段)', len(users_list) > 0 and all('password' not in u for u in users_list), str(list(users_list[0].keys()) if users_list else ''))
check('用户列表无身份证号字段', all('idCardNo' not in u for u in users_list), '')
s, r = call('PUT', '/user/6/status', admin_token, {'status': 'disabled'})
check('管理员禁用 tenant2', s == 200, f"status={s} {r}")
s, r = call('GET', '/api/appointments', t2_token)
check('被禁用用户请求被 401 拒绝', s == 401, f"status={s}")
s, r = login('tenant2', '123456', 'TENANT')
check('被禁用用户登录被拒', r.get('code') != 200, str(r.get('msg')))
s, r = call('PUT', '/user/6/status', admin_token, {'status': 'normal'})
check('管理员解禁 tenant2', s == 200, f"status={s} {r}")
s, r = login('tenant2', '123456', 'TENANT')
check('解禁后可重新登录', r.get('code') == 200, str(r.get('msg')))
s, r = call('GET', '/user', admin_token)
admin_id = [u for u in r['data']['users'] if u['role'] == 'ADMIN'][0]['id']
s, r = call('DELETE', f'/user/{admin_id}', admin_token)
check('管理员不能删除自己', s == 400, f"status={s} {r}")
s, r = call('GET', '/user', tenant_token)
s, r = call('GET', '/user', renter_token)
check('租客访问用户列表被拒(403)', s == 403, f"status={s}")

print()
print("=" * 60)
print("八、未认证 / 无效 Token")
print("=" * 60)
s, r = call('GET', '/api/appointments', token='invalid.token.here')
check('无效 Token 返回 401', s == 401, f"status={s}")
s, r = call('GET', '/api/notifications')
check('未登录请求通知返回 401', s == 401, f"status={s}")

print()
print("=" * 60)
print("九、清理测试数据（避免污染演示库）")
print("=" * 60)
# 管理员删除 E2E 注册的两个账号：级联清理其房源/预约/收藏/聊天/通知/申请单
for uid_name, uid in [('E2E房东账号', e2e_landlord_id), ('E2E租客账号', renter_id)]:
    s, r = call('DELETE', f'/user/{uid}', admin_token)
    check(f'清理{uid_name}(级联删除名下数据)', s == 200 and r.get('success'), f"status={s} {r}")
# 验证首页不再有 E2E 房源
s, r = call('GET', '/api/public/houses?page=1&pageSize=100')
check('演示库已无 E2E 测试房源', all('E2E' not in (h.get('title') or '') for h in r['data']['houses']),
      str([h['title'] for h in r['data']['houses'] if 'E2E' in (h.get('title') or '')]))

print()
print("=" * 50)
print(f"总计: PASS={len(PASS)}  FAIL={len(FAIL)}")
if FAIL:
    print("失败项:")
    for f in FAIL: print("  -", f)
sys.exit(1 if FAIL else 0)
