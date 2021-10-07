package cn.tjgzy.community.service;

import cn.tjgzy.community.entity.DiscussPost;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * @author GongZheyi
 * @create 2021-10-06-20:01
 */
@Service
public class ElasticSearchService {

    public void saveDiscussPost(DiscussPost post) {

    }

    public void deleteDiscussPost(int id) {

    }

    /**
     *
     * @param keyword
     * @param current 第几页
     * @param limit 每页的数量
     * @return
     */
    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        return null;
    }


}
