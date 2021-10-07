package cn.tjgzy.community.service;

import cn.tjgzy.community.dao.elasticsearch.DiscussPostRepository;
import cn.tjgzy.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author GongZheyi
 * @create 2021-10-06-20:01
 */
@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    public void saveDiscussPost(DiscussPost post) {
        discussRepository.save(post);
    }

    public void deleteDiscussPost(int id) {
        discussRepository.deleteById(id);
    }

    /**
     *
     * @param keyword
     * @param current 第几页
     * @param limit 每页的数量
     * @return
     */
    public SearchHits<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        SearchHits<DiscussPost> search = elasticsearchOperations.search(query, DiscussPost.class);
//        long totalHits = search.getTotalHits();
//        System.out.println(totalHits);

        /**
         * 封装高亮数据
         */
        for (SearchHit<DiscussPost> hit : search) {
            DiscussPost post = hit.getContent();
            List<String> title = hit.getHighlightField("title");
            if (title != null && title.size() > 0) {
                post.setTitle(title.get(0));
            }
            List<String> content = hit.getHighlightField("content");
            if (content != null && content.size() > 0) {
                post.setContent(content.get(0));
            }
        }
        return search;
    }


}
