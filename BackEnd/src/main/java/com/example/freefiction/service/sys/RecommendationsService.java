package com.example.freefiction.service.sys;

import com.example.freefiction.entity.Recommendations;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.freefiction.handler.ServiceResult;
import org.springframework.stereotype.Service;

/**
* @author Tjianwei
* @description 针对表【recommendations(推荐表)】的数据库操作Service
* @createDate 2025-11-24 10:48:44
*/
@Service
public interface RecommendationsService extends IService<Recommendations> {
    /**
     * 添加推荐
     * @param recommendations 小说ID
     * @return 是否添加成功
     */
    ServiceResult<Boolean> addRecommendations(Recommendations  recommendations);

    /**
     * 删除推荐
     * @param novelId 小说ID
     * @return 是否删除成功
     */
    ServiceResult<Boolean> deleteRecommendations(Long novelId);
}
