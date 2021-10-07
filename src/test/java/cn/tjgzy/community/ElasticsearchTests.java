package cn.tjgzy.community;

import cn.tjgzy.community.dao.DiscussPostMapper;
import cn.tjgzy.community.dao.elasticsearch.DiscussPostRepository;
import cn.tjgzy.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.thymeleaf.spring5.context.SpringContextUtils;

import java.util.List;

/**
 * @author GongZheyi
 * @create 2021-10-07-9:03
 */
@SpringBootTest
public class ElasticsearchTests {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;


    @Test
    public void testInsert() {
        discussRepository.save(discussPostMapper.selectDiscussPostById(110));
        discussRepository.save(discussPostMapper.selectDiscussPostById(282));
        discussRepository.save(discussPostMapper.selectDiscussPostById(281));
        discussRepository.save(discussPostMapper.selectDiscussPostById(277));
    }

    @Test
    public void testInsertList() {
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(101,0,100));
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(102,0,100));
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(103,0,100));
    }

    @Test
    public void insertAll() {
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(0,0,Integer.MAX_VALUE));
    }

    @Test
    public void testDelete() {
        discussRepository.deleteById(282);
    }

    @Test
    public void deleteAll() {
        discussRepository.deleteAll();
    }

    @Test
    public void testSearchByRepository() {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,2))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("<em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("<em>")
                ).build();
        SearchHits<DiscussPost> search = elasticsearchOperations.search(query, DiscussPost.class);
        long totalHits = search.getTotalHits();
        System.out.println("一共有" + totalHits);
        List<SearchHit<DiscussPost>> searchHits = search.getSearchHits();
        /**
         * 封装数据
         */
        for (SearchHit<DiscussPost> hit : searchHits) {
            DiscussPost post = hit.getContent();
            List<String> title = hit.getHighlightField("title");
            if (title != null && title.size() > 0) {
                post.setTitle(title.get(0));
            }
            List<String> content = hit.getHighlightField("content");
            if (content != null && content.size() > 0) {
                post.setContent(content.get(0));
            }
            System.out.println(post);
        }
    }
}
