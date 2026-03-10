package framework.admin.service.house.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import framework.admin.api.appuser.domain.DTO.AppUserDTO;
import framework.admin.api.config.domain.DTO.DicDataDTO;
import framework.admin.api.house.domain.DTO.DeviceDTO;
import framework.admin.api.house.domain.DTO.TagDTO;
import framework.admin.service.config.service.DicService;
import framework.admin.service.house.domain.DTO.HouseAddOrEditReqDTO;
import framework.admin.service.house.domain.DTO.HouseDTO;
import framework.admin.service.house.domain.entity.*;
import framework.admin.service.house.domain.enums.HouseStatusEnum;
import framework.admin.service.house.mapper.*;
import framework.admin.service.house.service.HouseService;
import framework.admin.service.map.domain.entity.SysRegion;
import framework.admin.service.map.mapper.RegionMapper;
import framework.admin.service.map.service.MapService;
import framework.admin.service.user.domain.entity.AppUser;
import framework.admin.service.user.mapper.AppUserMapper;
import framework.admin.service.user.service.AppUserService;
import framework.core.utils.BeanCopyUtil;
import framework.core.utils.JsonUtils;
import framework.domain.ServiceException;
import framework.redis.service.RedisService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HouseServiceImpl implements HouseService {

    @Autowired
    private CityHouseMapper cityHouseMapper;
    @Autowired
    private HouseMapper houseMapper;
    @Autowired
    private HouseStatusMapper houseStatusMapper;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private TagHouseMapper tagHouseMapper;
    @Autowired
    private AppUserService  appUserService;
    @Autowired
    private DicService dicService;
    @Autowired
    private RegionMapper regionMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private AppUserMapper appUserMapper;


    // 城市房源映射 key 前缀
    private static final String CITY_HOUSE_PREFIX = "house:list:";
    // 城市完整信息 key 前缀
    private static final String HOUSE_PREFIX = "house:";

    @Override
    @Transactional
    public Long addOrEdit(HouseAddOrEditReqDTO houseAddOrEditReqDTO) {

        //1. 校验参数
        checkAddOrEditReq(houseAddOrEditReqDTO);

        //2. 组装新增/编辑的参数
        House house = new House();
        house.setUserId(houseAddOrEditReqDTO.getUserId());
        house.setTitle(houseAddOrEditReqDTO.getTitle());
        house.setRentType(houseAddOrEditReqDTO.getRentType());
        house.setFloor(houseAddOrEditReqDTO.getFloor());
        house.setAllFloor(houseAddOrEditReqDTO.getAllFloor());
        house.setHouseType(houseAddOrEditReqDTO.getHouseType());
        house.setRooms(houseAddOrEditReqDTO.getRooms());
        house.setPosition(houseAddOrEditReqDTO.getPosition());
        house.setArea(BigDecimal.valueOf(houseAddOrEditReqDTO.getArea()));
        house.setPrice(BigDecimal.valueOf(houseAddOrEditReqDTO.getPrice()));
        house.setIntro(houseAddOrEditReqDTO.getIntro());
        house.setCityId(houseAddOrEditReqDTO.getCityId());
        house.setCityName(houseAddOrEditReqDTO.getCityName());
        house.setRegionId(houseAddOrEditReqDTO.getRegionId());
        house.setRegionName(houseAddOrEditReqDTO.getRegionName());
        house.setCommunityName(houseAddOrEditReqDTO.getCommunityName());
        house.setDetailAddress(houseAddOrEditReqDTO.getDetailAddress());
        house.setLongitude(BigDecimal.valueOf(houseAddOrEditReqDTO.getLongitude()));
        house.setLatitude(BigDecimal.valueOf(houseAddOrEditReqDTO.getLatitude()));
        house.setHeadImage(houseAddOrEditReqDTO.getHeadImage());

        //需要特殊处理的参数
        //图片地址转json
        house.setImages(JsonUtils.ObjectToString(houseAddOrEditReqDTO.getImages()));

        //devices使用逗号分隔开

        house.setDevices(houseAddOrEditReqDTO.getDevices().stream()
                .filter(s->!s.isEmpty())
                .collect(Collectors.joining(",")));

        //3. 编辑操作
        if (houseAddOrEditReqDTO.getHouseId() != null) {

            house.setId(houseAddOrEditReqDTO.getHouseId());
            House exitHouse = houseMapper.selectById(houseAddOrEditReqDTO.getHouseId());

            //3.1 检查CityHouse参数是否需要修改
            //3.2 如果需要修改需要同步修改redis和mysql
            if (cityHouseNeedChange(exitHouse, houseAddOrEditReqDTO.getCityId())) {
                editCityHouse(house.getId(), house.getCityId(),
                        exitHouse.getCityId(), house.getCityName());
            }

            //3.3 检查TagHouse是否需要修改
            //3.4 修改只需要修改mysql，因为已经存储了一个完整的house
            List<TagHouse> tagHouses = tagHouseMapper.selectList(new LambdaQueryWrapper<TagHouse>()
                    .eq(TagHouse::getHouseId, house.getId()));

            if (TagHouseNeedChange(tagHouses, houseAddOrEditReqDTO.getTagCodes())) {
                editTagHouse(house.getId(),tagHouses, houseAddOrEditReqDTO.getTagCodes());
            }
        }

        //执行插入修改逻辑，因为如果是新增插入后才有id产生
        houseMapper.insertOrUpdate(house);

        //4. 新增操作
        if (houseAddOrEditReqDTO.getHouseId() == null) {
            //4.1 新增CityHouse
            //4.2 新增TagHouse
            //4.3 新增HouseStatus
            HouseStatus houseStatus = new HouseStatus();
            houseStatus.setHouseId(house.getId());
            houseStatus.setStatus(HouseStatusEnum.UP.name());
            houseStatusMapper.insert(houseStatus);

            //mysql + redis
            addCityHouse(house.getId(), house.getCityName(), house.getCityId());

            //mysql
            addTagHouse(house.getId(), houseAddOrEditReqDTO.getTagCodes());
        }

        //5. 缓存完整的House信息
        cacheHouse(house.getId());
        return 0L;
    }

    /**
     * 缓存house信息
     * @param id
     */
    private void cacheHouse(Long id) {
        if (id == null) {
             log.error("id为空");
             return;
        }

        HouseDTO houseDTO = getHouseDTObyId(id);
        if (houseDTO == null) {
            log.error("查询房源信息错误");
            return;
        }

        cacheHouse(houseDTO);
    }

    /**
     * 根据房源id获取HouseDTO
     * @param houseId
     * @return
     */
    private HouseDTO getHouseDTObyId(Long houseId) {
        if (null == houseId) {
            log.warn("要查询的房源id为空");
            return null;
        }

        // 查房源、状态、tagHouse关联关系、房东信息
        House house = houseMapper.selectById(houseId);
        if (null == house) {
            log.error("查询房源失败，houseId:{}", houseId);
            return null;
        }

        AppUser appUser = appUserMapper.selectById(house.getUserId());
        if (null == appUser) {
            log.error("查询的房源房东信息不存在，houseId:{}, userId:{}", houseId, house.getUserId());
            return null;
        }

        HouseStatus houseStatus = houseStatusMapper.selectOne(
                new LambdaQueryWrapper<HouseStatus>()
                        .eq(HouseStatus::getHouseId, houseId));
        if (null == houseStatus) {
            log.error("查询的房源状态信息不存在，houseId:{}", houseId);
            return null;
        }

        List<TagHouse> tagHouses = tagHouseMapper.selectList(
                new LambdaQueryWrapper<TagHouse>().eq(TagHouse::getHouseId, houseId));


        // 组装完整的房源信息
        return convertToHouseDTO(house, houseStatus, appUser, tagHouses);
    }

    /**
     * 组装房源完整信息
     *
     * @param house
     * @param houseStatus
     * @param appUser
     * @param tagHouses
     * @return
     */
    private HouseDTO convertToHouseDTO(House house, HouseStatus houseStatus,
                                       AppUser appUser, List<TagHouse> tagHouses) {
        // 校验数据合法性
        if (null == house || null == houseStatus || null == appUser) {
            log.warn("房源信息不完整！");
            return null;
        }

        HouseDTO houseDTO = new HouseDTO();
        BeanUtils.copyProperties(house, houseDTO);
        BeanUtils.copyProperties(houseStatus, houseDTO);
        BeanUtils.copyProperties(appUser, houseDTO);

        houseDTO.setArea(house.getArea().doubleValue());
        houseDTO.setPrice(house.getPrice().doubleValue());
        houseDTO.setLongitude(house.getLongitude().doubleValue());
        houseDTO.setLatitude(house.getLatitude().doubleValue());
        houseDTO.setImages(JsonUtils.StringToList(house.getImages(), String.class));

        // 表： soft,washer,broadband
        // DeviceDTO:  String deviceCode，String deviceName;
        List<String> dataKeys = Arrays.stream(house.getDevices().split(","))
                .distinct()
                .collect(Collectors.toList());

        List<DicDataDTO> deviceDataDTOS
                = dicService.getDicDataByKeys(dataKeys);

        List<DeviceDTO> deviceDTOS = deviceDataDTOS.stream()
                .map(dataDTO -> {
                    DeviceDTO deviceDTO = new DeviceDTO();
                    deviceDTO.setDeviceCode(dataDTO.getDataKey());
                    deviceDTO.setDeviceName(dataDTO.getValue());
                    return deviceDTO;
                }).collect(Collectors.toList());
        houseDTO.setDevices(deviceDTOS);


        // TagDTO:String tagCode; String tagName;
        // 表 Tag

        // 获取到tagCodes，接着查询Tag
        List<String> tagCodes = tagHouses.stream()
                .map(TagHouse::getTagCode)
                .distinct()
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(tagCodes)) {
            List<Tag> tags = tagMapper.selectList(
                    new LambdaQueryWrapper<Tag>().in(Tag::getTagCode, tagCodes));
            houseDTO.setTags(BeanCopyUtil.copyListProperties(tags, TagDTO::new));
        }

        return houseDTO;

    }

    /**
     * 根据dto缓存
     * @param houseDTO
     */
    private void cacheHouse(HouseDTO houseDTO) {
        if (null == houseDTO) {
            log.warn("要缓存的房源详细信息为空！");
            return;
        }

        // 缓存
        try {
            redisService.setCacheObject(HOUSE_PREFIX + houseDTO.getHouseId(),
                    JsonUtils.ObjectToString(houseDTO));
        } catch (Exception e) {
            log.error("缓存房源完整信息时发生异常，houseDTO:{}", JsonUtils.ObjectToString(houseDTO), e);
            // 对于房源完整信息，是否存在于redis，不需要强一致性。
            // 因为C端查询时，如果redis不存在，可以通过查MySQL获取到数据，让后再放入Redis。
        }
    }

    /**
     * 新增cityHouse
     */
    private void addCityHouse(Long houseId, String cityName, Long cityId) {
        CityHouse cityHouse = new CityHouse();
        cityHouse.setHouseId(houseId);
        cityHouse.setCityName(cityName);
        cityHouse.setCityId(cityId);

        cityHouseMapper.insert(cityHouse);

        cacheCityHouse(2, null, cityId, houseId);
    }

    /**
     * 添加TagHouse
     * @param houseId
     * @param tagCodes
     */
    private void addTagHouse(Long houseId, List<String> tagCodes) {
        List<TagHouse> insertDatas = tagCodes.stream()
                .filter(s -> !s.isEmpty())
                .distinct()
                .map(code -> {
                    TagHouse tagHouse = new TagHouse();
                    tagHouse.setHouseId(houseId);
                    tagHouse.setTagCode(code);
                    return tagHouse;
                }).collect(Collectors.toList());

        tagHouseMapper.insert(insertDatas);
    }

    /**
     * 更新TagHouse表
     * @param houseId
     * @param oldTagHouses
     * @param newTagCode
     */
    private void editTagHouse(Long houseId, List<TagHouse> oldTagHouses,
                              List<String> newTagCode) {
        //1. 新增那些部分，删除那些部分
        //2. 转换
        List<String> oldTagCodes = oldTagHouses.stream()
                .map(TagHouse::getTagCode)
                .distinct()
                .collect(Collectors.toList());

        //3. 找到要删除的
        List<String> deleteCodes = oldTagCodes.stream()
                .filter(tagCode -> !newTagCode.contains(tagCode))
                .collect(Collectors.toList());

        //4. 找到要新增的code
        List<String> addCodes = newTagCode.stream()
                .filter(tagCode -> !oldTagCodes.contains(tagCode))
                .collect(Collectors.toList());

        //构建新增
        List<TagHouse> addTagHouses = addCodes.stream()
                .map(code -> {
                    TagHouse tagHouse = new TagHouse();
                    tagHouse.setTagCode(code);
                    tagHouse.setHouseId(houseId);
                    return tagHouse;
                })
                .collect(Collectors.toList());

        //5. 执行删除操作
        if (CollectionUtils.isNotEmpty(deleteCodes)) {
            tagHouseMapper.delete(new LambdaQueryWrapper<TagHouse>()
                    .eq(TagHouse::getHouseId, houseId)
                    .in(TagHouse::getTagCode, deleteCodes));
        }

        //6. 执行新增操作
        if (CollectionUtils.isNotEmpty(addTagHouses)) {
            tagHouseMapper.insert(addTagHouses);
        }
    }

    /**
     * 修改mysql和redis的存储
     * @param houseId
     * @param newCityId
     * @param oldCityId
     * @param cityName
     */
    public void editCityHouse(Long houseId, Long newCityId, Long oldCityId,
                                String cityName) {
        //1. 修改mysql
        //删除原来的信息
        cityHouseMapper.delete(new LambdaQueryWrapper<CityHouse>()
                .eq(CityHouse::getHouseId, houseId));
        //2. 新增的信息
        CityHouse cityHouse = new CityHouse();
        cityHouse.setHouseId(houseId);
        cityHouse.setCityId(newCityId);
        cityHouse.setCityName(cityName);

        cityHouseMapper.insert(cityHouse);

        cacheCityHouse(1, oldCityId, newCityId, houseId);
    }

    /**
     * 修改或者新增cityHouse
     * @param op
     * @param oldCityId
     * @param newCityId
     * @param houseId
     */
    public void cacheCityHouse(int op, Long oldCityId, Long newCityId, Long houseId) {
        try {
            //修改
            if (op == 1) {
                redisService.leftPopForList(CITY_HOUSE_PREFIX + oldCityId, houseId);
                redisService.rightPushForList(CITY_HOUSE_PREFIX + newCityId, houseId);
            }
            //新增
            else if (op == 2) {
                redisService.rightPushForList(CITY_HOUSE_PREFIX + newCityId, houseId);
            } else {
                log.error("选择有误,op{},oldCityId{}, houseId{}", op, oldCityId, houseId);
            }
        } catch (Exception e) {
            log.error("redis缓存异常,op{},oldCityId{}, houseId{}", op, oldCityId, houseId);
            //抛出异常，这里要和数据库保持一致性
            throw e;
        }

    }

    /**
     * 判断城市->房源表是否需要更新
     * @param house
     * @param cityId
     * @return
     */
    private boolean cityHouseNeedChange(House house, Long cityId) {
        return !house.getCityId().equals(cityId);
    }

    /**
     * 判断tag->house表是否需要更新
     * @param oldTags
     * @param newTags
     * @return
     */
    public boolean TagHouseNeedChange(List<TagHouse> oldTags,  List<String> newTags) {
        //转换成string，并且排序
        List<String> oldCodes = oldTags.stream()
                .map(TagHouse::getTagCode)
                .sorted()
                .collect(Collectors.toList());

        newTags = newTags.stream().sorted().collect(Collectors.toList());
        return oldCodes.equals(newTags);
    }

    /**
     * 校验参数的函数
     * @param houseAddOrEditReqDTO
     */
    private void checkAddOrEditReq(HouseAddOrEditReqDTO houseAddOrEditReqDTO) {
        if (houseAddOrEditReqDTO == null) {
            throw new ServiceException("请求参数为空");
        }
        //1. 房东id是否存在
        AppUserDTO userId = appUserService.findById(houseAddOrEditReqDTO.getUserId());
        if (userId == null) {
            throw new ServiceException("房东id不存在");
        }
        //2. 城市id是否存在
        //3. 区域id是否存在
        List<Long> list = Arrays.asList(
                houseAddOrEditReqDTO.getRegionId(),
                houseAddOrEditReqDTO.getCityId()
        );
        List<SysRegion> regions = regionMapper.selectBatchIds(list);
        if (regions.size() != list.size()) {
            throw new ServiceException("区域/城市id错误");
        }
        //4. 设备列表是否都存在
        List<DicDataDTO> devices = dicService.getDicDataByKeys(houseAddOrEditReqDTO.getDevices());
        if (devices.size() != list.size()) {
            throw new ServiceException("设备存在非法信息");
        }
        //5. 标签列表是否都存在
        List<Tag> tags = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                .in(Tag::getTagCode, houseAddOrEditReqDTO.getTagCodes()));
        if (tags.size() != houseAddOrEditReqDTO.getTagCodes().size()) {
            throw new ServiceException("标签存在非法信息");
        }
        //6. 朝向是否存在，出租类型是否存在
        if (dicService.getDicDataByKey(houseAddOrEditReqDTO.getPosition()) == null) {
            throw new ServiceException("朝向不存在");
        }
        if (dicService.getDicDataByKey(houseAddOrEditReqDTO.getRentType()) == null) {
            throw new ServiceException("出租类型不存在");
        }
        //TODO 验证cityId对应的name是否匹配

    }

}
