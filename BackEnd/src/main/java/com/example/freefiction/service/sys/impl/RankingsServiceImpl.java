package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.entity.Rankings;
import com.example.freefiction.service.sys.RankingsService;
import com.example.freefiction.mapper.RankingsMapper;
import org.springframework.stereotype.Service;

/**
* @author Tjianwei
* @description 针对表【rankings(排行榜表)】的数据库操作Service实现
* @createDate 2025-11-24 10:48:52
*/
@Service
public class RankingsServiceImpl extends ServiceImpl<RankingsMapper, Rankings>
    implements RankingsService{

}




