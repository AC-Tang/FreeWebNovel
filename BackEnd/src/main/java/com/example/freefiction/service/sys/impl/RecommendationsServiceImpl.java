package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.entity.Recommendations;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.RedisService;
import com.example.freefiction.service.sys.RecommendationsService;
import com.example.freefiction.mapper.RecommendationsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
* @author Tjianwei
* @description 针对表【recommendations(推荐表)】的数据库操作Service实现
* @createDate 2025-11-24 10:48:44
*/
@Service
@RequiredArgsConstructor
public class RecommendationsServiceImpl extends ServiceImpl<RecommendationsMapper, Recommendations>
    implements RecommendationsService{
    private final RedisService redisService;

    @Override
    public ServiceResult<Boolean> addRecommendations(Recommendations  recommendations){
        boolean result = this.saveOrUpdate(recommendations);
        if(!result){
            return ServiceResult.fail("添加推荐失败");
        }
        return ServiceResult.ok(true);
    }

    @Override
    public ServiceResult<Boolean> deleteRecommendations(Long novelId){
        boolean result = this.removeById(novelId);
        if(!result){
            return ServiceResult.fail("删除推荐失败");
        }
        redisService.delete("recommendations:Book" + novelId);
        return ServiceResult.ok(true);
    }
}




