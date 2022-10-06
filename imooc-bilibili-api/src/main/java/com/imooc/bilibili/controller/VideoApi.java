package com.imooc.bilibili.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.controller.support.UserSupport;
import com.imooc.bilibili.domain.*;
import com.imooc.bilibili.service.ElasticSearchService;
import com.imooc.bilibili.service.VideoService;
import com.imooc.bilibili.service.util.FastDFSUtil;
import org.apache.mahout.cf.taste.common.TasteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class VideoApi {
    @Autowired
    private UserSupport userSupport;
    @Autowired
    private VideoService videoService;

    @Autowired
    private ElasticSearchService elasticSearchService;
    @Autowired
    private FastDFSUtil fastDFSUtil;

    /**
     * 视频投稿
     * @param video
     * @return
     */
    @PostMapping("/videos")
    public JsonResponse<String>addVideos(@RequestBody Video video){
        Long userId = userSupport.getCurrentUserId();
        video.setUserId(userId);
        videoService.addVideos(video);
        elasticSearchService.addVideo(video);
        return JsonResponse.success();
    }

    /**
     * 视频分页
     * @param size
     * @param no
     * @param area
     * @return
     */
    @GetMapping("/videos")
    public JsonResponse<PageResult<Video>>pageListVideos(Integer size,Integer no,String area){
    PageResult<Video>result = videoService.pageListVideo(size,no,area);
    return new JsonResponse<>(result);
    }

    /**
     * 视频分片上传
     * @param request
     * @param response
     * @param url
     * @throws Exception
     */
    @GetMapping("/video-slices")
    public void viewVideoOnlineBySlices(HttpServletRequest request,
                                        HttpServletResponse response,
                                        String url) throws Exception {
        fastDFSUtil.viewVideoOnlineBySlices(request,response,url);
    }

    @PostMapping("/video-tags")
    public JsonResponse<String>addVideoTags(@RequestBody VideoTag videoTag){
        Long userId = userSupport.getCurrentUserId();
        videoTag.setId(userId);
        videoService.addVideoTags(videoTag);
        return JsonResponse.success();
    }

    /**
     * 查询对应的标签
     * @param videoId
     * @return
     */
    @GetMapping("/video-tags")
    public JsonResponse<List<Tag>>getVideoTags(@RequestParam Long videoId){
        List<Tag>list =  videoService.getVideoTags(videoId);
        return new JsonResponse<>(list);
    }

    /**
     * 删除视频标签
     * @param params
     * @return
     */
    @DeleteMapping("/video-tags")
    public JsonResponse<String>deleteVideoTags(@RequestBody JSONObject params){
        String tagList = params.getString("tagIdList");
        Long videoId =  params.getLong("videoId");
        videoService.deleteVideoTags(JSONArray.parseArray(tagList).toJavaList(Long.class),videoId);
        return JsonResponse.success();
    }

    /**
     * 视频点赞
     * @param videoId
     * @return
     */
    @PostMapping("/video-like")
    public JsonResponse<String>addVideoLike(@RequestParam Long videoId){
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoLike(videoId,userId);
        return JsonResponse.success();
    }

    /**
     * 取消点赞
     * @param videoId
     * @return
     */
    @DeleteMapping("/video-likes")
    public JsonResponse<String>deleteVideoLike(@RequestParam Long videoId){
        Long userId = userSupport.getCurrentUserId();
        videoService.deleteVideoLike(videoId,userId);
        return JsonResponse.success();
    }

    /**
     * 查询点赞数
     * @param videoId
     * @return
     */
    @GetMapping("/video-likes")
    public JsonResponse<Map<String,Object>>getVideoLike(@RequestParam Long videoId){
        //需要进行游客和登陆用户的判断
        Long userId = null;
        try{
        userId = userSupport.getCurrentUserId();
        }catch (Exception ignore){}
        Map<String,Object>map =  videoService.getVideoLike(videoId,userId);
        return new JsonResponse<>(map);
    }

    /**
     * 添加收藏
     * @param videoCollection
     * @return
     */
    @PostMapping("/video-collections")
    public JsonResponse<String>addVideoCollection(@RequestBody VideoCollection videoCollection){
        Long userId = userSupport.getCurrentUserId();
        videoCollection.setId(userId);
        videoService.addVideoCollection(videoCollection,userId);
        return JsonResponse.success();
    }

    /**
     * 取消收藏
     * @param videoId
     * @return
     */
    @DeleteMapping("/video-collections")
    public JsonResponse<String>deleteVideoCollection(@RequestParam Long videoId){
        Long userId = userSupport.getCurrentUserId();
        videoService.deleteVideoCollection(videoId,userId);
        return JsonResponse.success();
    }

    /**
     * requestParam可以自带判断非空验证
     * 查寻点赞数量
     * @param videoId
     * @return
     */
    @GetMapping("/video-collections")
     public JsonResponse<Map<String,Object>>getVideoCollections(@RequestParam Long videoId){
         Long userId = null;
         try{
             userId = userSupport.getCurrentUserId();
         }catch (Exception ignored){}
         Map<String,Object>result =  videoService.getVideoCollections(videoId,userId);
         return new JsonResponse<>(result);

     }

    /**
     * 添加视频投币
     * @param videoCoin
     * @return
     */
     @PostMapping("/video-coins")
     public JsonResponse<String>addVideoCoins(@RequestBody VideoCoin videoCoin){
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoCoins(videoCoin,userId);
        return JsonResponse.success();
     }

    /**
     * 查询视频投币
     * @param videoId
     * @return
     */
     @GetMapping("/video-coins")
     public JsonResponse<Map<String,Object>>getVideoCoins(@RequestParam Long videoId){
         Long userId = null;
         try{
             userId = userSupport.getCurrentUserId();
         }catch (Exception ignored){}
         Map<String, Object> result = videoService.getVideoCoins(videoId, userId);
         return new JsonResponse<>(result);
     }

    /**
     * 添加评论
     * @param videoComment
     * @return
     */
     @PostMapping("/video-comments")
     public JsonResponse<String>addVideoComment(@RequestBody VideoComment videoComment){
         Long userId = userSupport.getCurrentUserId();
         videoService.addVideoComment(videoComment,userId);
         return JsonResponse.success();
     }

     @GetMapping("/video-comments")
     public JsonResponse<PageResult<VideoComment>>pageListVideoComments(@RequestParam Integer size,
                                                                        @RequestParam Integer no,
                                                                        @RequestParam Long videoId){
         PageResult<VideoComment>result = videoService.pageListVideoComments(size,no,videoId);
         return new JsonResponse<>(result);

     }


    /**
     * 查询视频详细信息
     * @param videoId
     * @return
     */
     @GetMapping("/video-details")
     public JsonResponse<Map<String,Object>>getVideoDetails(Long videoId){
         Map<String,Object>result = videoService.getVideoDetails(videoId);
         return new JsonResponse<>(result);
     }

    /**
     * 添加视频观看记录
     */
    @PostMapping("/video-views")
    public JsonResponse<String>addVideoView(@RequestBody VideoView videoView,HttpServletRequest request){
        Long userId;
        try{
            userId = userSupport.getCurrentUserId();
            videoView.setUserId(userId);
            videoService.addVideoView(videoView,request);
        }catch (Exception e){
            videoService.addVideoView(videoView,request);
        }
        return JsonResponse.success();
    }

    /**
     * 查看视频观看人数记录
     * @param videoId
     * @return
     */
    @GetMapping("/video-view-counts")
    public JsonResponse<Integer>getVideoViewCounts(@RequestParam Long videoId){
        Integer count = videoService.getVideoViewCount(videoId);
        return new JsonResponse<>(count);
    }

    /**
     * 根据用户喜好推送视频
     * @return
     */
    @GetMapping("/recommendations")
    public JsonResponse<List<Video>>recommend() throws TasteException {
        Long userId = userSupport.getCurrentUserId();
        List<Video>list = videoService.recommend(userId);
        return new JsonResponse<>(list);
    }

    /**
     * 视频帧截取生成黑白剪影
     */
    @GetMapping("/video-frames")
    public JsonResponse<List<VideoBinaryPicture>> captureVideoFrame(@RequestParam Long videoId,
                                                                    @RequestParam String fileMd5) throws Exception {
        List<VideoBinaryPicture> list = videoService.convertVideoToImage(videoId, fileMd5);
        return new JsonResponse<>(list);
    }

    /**
     * 查询视频黑白剪影
     */
    @GetMapping("/video-binary-images")
    public JsonResponse<List<VideoBinaryPicture>> getVideoBinaryImages(@RequestParam Long videoId,
                                                                       Long videoTimestamp,
                                                                       String frameNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("videoId", videoId);
        params.put("videoTimestamp", videoTimestamp);
        params.put("frameNo", frameNo);
        List<VideoBinaryPicture> list = videoService.getVideoBinaryImages(params);
        return new JsonResponse<>(list);
    }

}
