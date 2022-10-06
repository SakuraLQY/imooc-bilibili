package com.imooc.bilibili.service;

import com.imooc.bilibili.dao.repository.UserInfoRepository;
import com.imooc.bilibili.dao.repository.VideoRepository;
import com.imooc.bilibili.domain.UserInfo;
import com.imooc.bilibili.domain.Video;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class ElasticSearchService {
    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private RestHighLevelClient client;

    public void addVideo(Video video){
        videoRepository.save(video);
    }

    public Video getVideo(String keyword){
        return videoRepository.findByTitleLike(keyword);
    }

    public void deleteAllVideos(){
        videoRepository.deleteAll();
    }

    public void addUserInfo(UserInfo userInfo){
        userInfoRepository.save(userInfo);
    }

    //分页请求
    public List<Map<String,Object>>getContents(String keyword,
                                               Integer pageNo,
                                               Integer pageSize) throws IOException {
        String[]indices = new String[]{"video","user-infos"};
        SearchRequest searchRequest = new SearchRequest(indices);//构建request
        //构建分页
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.from(pageNo-1);
        sourceBuilder.size(pageSize);
        MultiMatchQueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery(keyword,"title","nick","description");
        sourceBuilder.query(matchQueryBuilder);
        searchRequest.source(sourceBuilder);
        //高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        String[]array = {"title","nick","description"};
        for(String key:array){
            highlightBuilder.fields().add(new HighlightBuilder.Field(key));
        }
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style=\"color:red\">");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);
        //解析结果
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        List<Map<String,Object>>arrayList = new ArrayList<>();
        for(SearchHit hit:searchResponse.getHits()){
            //处理高亮字段
            Map<String, HighlightField>highLightBuilderFields = hit.getHighlightFields();//获取结果中高亮的部分
            Map<String,Object>sourceMap = hit.getSourceAsMap();
            for(String key:array){
                HighlightField field = highLightBuilderFields.get(key);
                if(field!=null){
                    Text[]fragments = field.getFragments();
                    String str = Arrays.toString(fragments);
                    str = str.substring(1,str.length()-1);
                    sourceMap.put(key,str);
                }
            }
            arrayList.add(sourceMap);
        }
            return arrayList;
    }
}
