package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.entity.SystemConfigs;
import com.example.freefiction.service.sys.SystemConfigsService;
import com.example.freefiction.mapper.SystemConfigsMapper;
import org.springframework.stereotype.Service;

/**
* @author Tjianwei
* @description 针对表【system_configs(系统配置表)】的数据库操作Service实现
* @createDate 2025-11-24 10:48:37
*/
@Service
public class SystemConfigsServiceImpl extends ServiceImpl<SystemConfigsMapper, SystemConfigs>
    implements SystemConfigsService{

}




