package cn.tjgzy.community.dao.elasticsearch;

import cn.tjgzy.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author GongZheyi
 * @create 2021-10-07-9:02
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {

}
