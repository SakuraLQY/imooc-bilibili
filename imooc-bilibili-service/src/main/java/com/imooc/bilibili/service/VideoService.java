package com.imooc.bilibili.service;

import com.imooc.bilibili.dao.VideoDao;
import com.imooc.bilibili.domain.*;
import com.imooc.bilibili.domain.exception.ConditionalException;
import com.imooc.bilibili.service.util.FastDFSUtil;
import com.imooc.bilibili.service.util.ImageUtil;
import com.imooc.bilibili.service.util.IpUtil;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoService {

    @Autowired
    private VideoDao videoDao;
    @Autowired
    private FastDFSUtil fastDFSUtil;
    @Autowired
    private UserCoinService userCoinService;
    @Autowired
    private UserService userService;
    @Autowired
    private FileService fileService;
    @Autowired
    private ImageUtil imageUtil;

    public static final int FRAME_NO = 32;
    @Transactional
    public void addVideos(Video video) {
        Date now = new Date();
        video.setCreateTime(new Date());
        videoDao.addVideos(video);
        Long videoId = video.getId();
        List<VideoTag> tagList = video.getVideoTagList();
        tagList.forEach(item->{
            item.setCreateTime(new Date());
            item.setVideoId(videoId);
        });
        videoDao.batchAddVideoTags(tagList);
    }

    /**
     * 视频列表分页
     * @param size
     * @param no
     * @param area
     * @return
     */
    public PageResult<Video>pageListVideo(Integer size,Integer no,String area){
        if(size ==null || no == null){
            throw new ConditionalException("异常参数");
        }
        Map<String,Object>params = new HashMap<>();
        params.put("start",(no-1)*size);
        params.put("limit",size);
        params.put("area",area);
        List<Video>list = new ArrayList<>();//查出的信息放在列表
        Integer total = videoDao.pageCountVideos(params);
        if(total>0){
            list = videoDao.pageListVideos(params);
        }
        return new PageResult<>(total,list);
    }

    /**
     * 在线视频流的观看
     */
    public void viewVideoOnlineBySlices(HttpServletRequest request,
                                        HttpServletResponse response,
                                        String url){
        try {
            fastDFSUtil.viewVideoOnlineBySlices(request,response,url);
        } catch (Exception ignored) {
        }
    }

    public void addVideoTags(VideoTag videoTag) {
        videoTag.setCreateTime(new Date());
        Long videoId = videoTag.getVideoId();
        if(videoId==null){
            throw new ConditionalException("视频失效！");
        }
        Long tagId = videoTag.getTagId();
        Tag tagInfo = new Tag();
        tagInfo.setId(tagId);
        tagInfo.setName(videoTag.getTag().getName());
        tagInfo.setCreateTime(new Date());
        videoDao.addVideoTags(tagInfo);
    }


    public List<Tag> getVideoTags(Long videoId){
        //或者该视频的所有tagId
        List<VideoTag>videoTagList = videoDao.getVideoTagList(videoId);
        Set<Long> tagList = videoTagList.stream().map(VideoTag::getTagId).collect(Collectors.toSet());
        List<Tag>tagInfoList = new ArrayList<>();
        if(tagList.size() > 0){
            tagInfoList = videoDao.getTagInfoList(tagList);
        }
        return tagInfoList;
    }

    public void deleteVideoTags(List<Long> tagList, Long videoId) {
        videoDao.deleteVideoTags(tagList,videoId);
    }

    //点赞
    public void addVideoLike(Long videoId, Long userId) {
        Video video =  videoDao.getVideoById(videoId);
        if(video==null){
            throw new ConditionalException("非法视频");
        }
        VideoLike videoLike =  videoDao.getVideoLikeByVideoIdAndUserId(videoId,userId);
        if(videoLike!=null){
            throw new ConditionalException("视频已点赞");
        }
        videoLike = new VideoLike();
        videoLike.setVideoId(videoId);
        videoLike.setUserId(userId);
        videoLike.setCreateTime(new Date());
        videoDao.addVideoLike(videoLike);
    }

    //取消点赞
    public void deleteVideoLike(Long videoId, Long userId) {
        videoDao.deleteVideoLike(videoId,userId);
    }

    //查看点赞数
    public Map<String, Object> getVideoLike(Long videoId, Long userId) {
        //Map里面装的是like count
        Long count = videoDao.getVideoLikes(videoId);
        VideoLike videoLike = videoDao.getVideoLikeByVideoIdAndUserId(videoId, userId);
        boolean like = videoLike!=null;
        Map<String,Object>result = new HashMap<>();
        result.put("count",count);
        result.put("like",like);
        return result;
    }

    @Transactional
    public void addVideoCollection(VideoCollection videoCollection, Long userId) {
        Long videoId = videoCollection.getVideoId();
        Long groupId = videoCollection.getGroupId();
        if(videoId==null || groupId==null){
            throw  new ConditionalException("异常参数");
        }
        //查询下video是否在
        Video video = videoDao.getVideoById(videoId);
        if(video==null){
            throw new ConditionalException("非法视频");
        }
        //删除原有视频收藏
        videoDao.deleteVideoCollection(videoId, userId);
        //添加新的视频收藏
        videoCollection.setUserId(userId);
        videoCollection.setCreateTime(new Date());
        videoDao.addVideoCollection(videoCollection);
    }

    public void deleteVideoCollection(Long videoId, Long userId) {
        videoDao.deleteVideoCollection(videoId,userId);
    }

    //查询收藏数量
    public Map<String, Object> getVideoCollections(Long videoId, Long userId) {
        Long count = videoDao.getVideoCollections(videoId);
        //查询是否收藏
        VideoCollection videoCollection = videoDao.getVideoCollectionByVideoIdAndUserId(videoId, userId);
        boolean like = videoCollection != null;
        Map<String, Object> result = new HashMap<>();
        result.put("count",count);
        result.put("like",like);
        return result;
    }

    @Transactional
    public void addVideoCoins(VideoCoin videoCoin, Long userId) {
        Integer amount = videoCoin.getAmount();//要投币的数量
        Long videoId = videoCoin.getVideoId();
        if(userId==null || videoId==null){
            throw new ConditionalException("非法参数");
        }
        Video video = videoDao.getVideoById(videoId);
        if(video==null){
            throw new ConditionalException("视频失效");
        }
        //查询当前用户硬币数量
        Integer userCoinAmount =  userCoinService.getUserCoinAmount(userId);
        userCoinAmount = userCoinAmount==null? 0:userCoinAmount;
        if(amount > userCoinAmount){
            throw new ConditionalException("账户硬币余额不足");
        }
        //开始进行投币，已经排除游客投币，余额不足
        //查询该用户对该视频投过多少币
        VideoCoin dbVideoCoin = videoDao.getVideoCoinByVideoIdAndUserId(videoId, userId);
        //新增投币数量
        if(dbVideoCoin==null){
            videoCoin.setUserId(userId);
            videoCoin.setCreateTime(new Date());
            videoDao.addVideoCoin(videoCoin);
        }else{
            Integer dbAmount = dbVideoCoin.getAmount();//视频硬币书
            dbAmount+=amount;
            videoCoin.setUserId(userId);
            videoCoin.setAmount(dbAmount);
            videoCoin.setUpdateTime(new Date());
            videoDao.updateVideoCoin(videoCoin);
        }
        userCoinService.updateUserCoinsAmount(userId,(userCoinAmount-amount));
    }

    //查询硬币数量
    public Map<String, Object> getVideoCoins(Long videoId, Long userId) {
        Long count = videoDao.getVideoCoinsAmount(videoId);
        VideoCoin videoCollection = videoDao.getVideoCoinByVideoIdAndUserId(videoId, userId);
        boolean like = videoCollection != null;
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("like", like);
        return result;
    }

    public void addVideoComment(VideoComment videoComment, Long userId) {
        Long videoId = videoComment.getVideoId();
        if(videoId == null){
            throw new ConditionalException("参数异常!");
        }
        Video video = videoDao.getVideoById(videoId);
        if(video==null){
            throw new ConditionalException("非法视频!");
        }
        videoComment.setUserId(userId);
        videoComment.setCreateTime(new Date());
        videoDao.addVideoComment(videoComment);
    }

    /**
     * 评论分页
     * @param size
     * @param no
     * @param videoId
     * @return
     */
    public PageResult<VideoComment> pageListVideoComments(Integer size, Integer no, Long videoId) {
        Video video = videoDao.getVideoById(videoId);
        if(video==null){
            throw new ConditionalException("非法视频!");
        }
        Map<String,Object>params = new HashMap<>();
        params.put("start",(no-1)*size);
        params.put("limit",size);
        params.put("videoId",videoId);
        //计算评论总数
        Integer total =  videoDao.pageCountVideoComments(params);
        List<VideoComment>list = new ArrayList<>();
        if(total > 0){
            //1.获得评论分页数据
            list = videoDao.pageListVideoComments(params);
            //2.批量查询二级评论
            List<Long>parentIdList = list.stream().map(VideoComment::getId).collect(Collectors.toList());
            List<VideoComment>childCommentList = videoDao.batchGetVideoCommentsByRootIds(parentIdList);//根据id->查询对应的rootIdList信息
            //批量查询用户信息
            Set<Long> userIdList = list.stream().map(VideoComment::getUserId).collect(Collectors.toSet());
//            Set<Long> replyUserId = childCommentList.stream().map(VideoComment::getUserId).collect(Collectors.toSet());
            Set<Long> replyUserIdList = childCommentList.stream().map(VideoComment::getReplyUserId).collect(Collectors.toSet());
            userIdList.addAll(replyUserIdList);
//            userIdList.addAll(childUserIdList);
            //根据评论下的各个userId查到对应的详细信息
            List<UserInfo>userInfoList = userService.batchGetUserInfoByUserIds(userIdList);
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getUserId, userInfo -> userInfo));
            //将一级评论下的二级评论也查出来
            list.forEach(comment->{
                Long id = comment.getId();
                List<VideoComment>childList = new ArrayList<>();
                childCommentList.forEach(child->{
                    if(id.equals(child.getRootId())){
                        child.setUserInfo(userInfoMap.get(child.getUserId()));
                        child.setReplyUserInfo(userInfoMap.get(child.getReplyUserId()));
                        childList.add(child);
                    }
                });
                comment.setChildList(childList);
                comment.setUserInfo(userInfoMap.get(comment.getUserId()));
            });
        }
            return new PageResult<>(total,list);
    }

    /**
     * 查询视频详情
     * @param videoId
     * @return
     */
    public Map<String, Object> getVideoDetails(Long videoId) {
        Video video = videoDao.getVideoDetails(videoId);
        Long userId = video.getUserId();
        if(video==null){
            throw new ConditionalException("非法视频");
        }
        //查询对应视频用户信息
        User user = userService.getUserById(userId);
        UserInfo userInfo = user.getUserInfo();
        Map<String,Object>params = new HashMap<>();
        params.put("video",video);
        params.put("userInfo",userInfo);
        return params;
    }

    public void addVideoView(VideoView videoView, HttpServletRequest request) {
        Long videoId = videoView.getVideoId();
        Long userId = videoView.getUserId();
        //生成clientId
        String agent = request.getHeader("User-Agent");
        UserAgent userAgent = UserAgent.parseUserAgentString(agent);
        String clientId = String.valueOf(userAgent.getId());
        String ip = IpUtil.getIP(request);
        Map<String,Object>params = new HashMap<>();
        if(userId != null){//不是访客
            params.put("userId",userId);
        }else{
            params.put("ip",ip);
            params.put("clientId",clientId);
        }
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        params.put("today",sdf.format(now));
        params.put("videoId",videoId);
        //查看是否存在
        VideoView dbVideoView = videoDao.getVideoView(params);
        //每天只需要记录一次观看记录
        if(dbVideoView== null){
            videoView.setIp(ip);
            videoView.setClientId(clientId);
            videoView.setCreateTime(new Date());
            videoDao.addVideoView(videoView);
        }
    }

    public Integer getVideoViewCount(Long videoId) {
        return videoDao.getVideoViewCount(videoId);
    }

    //获取用户喜好，进行视频的推送
    public List<Video> recommend(Long userId) throws TasteException {
        List<UserPreference>list = videoDao.getAllUserPreference();
        //创建模型数据
        DataModel dataModel = this.createDataModel(list);
        //2.创建similar相似度
        UserSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
        //3.获取用户userNeighborhood
        System.out.println(similarity.userSimilarity(7,8));
        UserNeighborhood userNeighborhood = new NearestNUserNeighborhood(2,similarity,dataModel);
        long[]ar = userNeighborhood.getUserNeighborhood(userId);
        //4.构建推荐器recommend
        Recommender recommender = new GenericUserBasedRecommender(dataModel,userNeighborhood,similarity);
        List<RecommendedItem>recommendedItems = recommender.recommend(userId,5);//展示类似的5个视频
        List<Long>itemIds = recommendedItems.stream().map(RecommendedItem::getItemID).collect(Collectors.toList());
        return videoDao.batchGetVideoByIds(itemIds);

    }

    private DataModel createDataModel(List<UserPreference> userPreferenceList) {
        FastByIDMap<PreferenceArray> fastByIdMap = new FastByIDMap<>();
        Map<Long, List<UserPreference>> map = userPreferenceList.stream().collect(Collectors.groupingBy(UserPreference::getUserId));
        Collection<List<UserPreference>> list = map.values();
        for(List<UserPreference> userPreferences : list){
            GenericPreference[] array = new GenericPreference[userPreferences.size()];
            for(int i = 0; i < userPreferences.size(); i++){
                UserPreference userPreference = userPreferences.get(i);
                GenericPreference item = new GenericPreference(userPreference.getUserId(), userPreference.getVideoId(), userPreference.getValue());
                array[i] = item;
            }
            fastByIdMap.put(array[0].getUserID(), new GenericUserPreferenceArray(Arrays.asList(array)));
        }
        return new GenericDataModel(fastByIdMap);
    }

    public List<VideoBinaryPicture> convertVideoToImage(Long videoId, String fileMd5) throws Exception {
        File file = fileService.getFileByMd5(fileMd5);
        String filePath = "E:\\WorkSpace\\tmp"+videoId + "."+ file.getType();
        fastDFSUtil.downLoadFile(file.getUrl(),filePath);
        FFmpegFrameGrabber fFmpegFrameGrabber = FFmpegFrameGrabber.createDefault(filePath);
        fFmpegFrameGrabber.start();//启动
        int ffLength = fFmpegFrameGrabber.getLengthInFrames();
        //获取帧
        Frame frame;
        Java2DFrameConverter converter = new Java2DFrameConverter();
        int count = 1;
        List<VideoBinaryPicture>pictures = new ArrayList<>();
        for(int i =1;i<=ffLength;i++){
            Long timestamp = fFmpegFrameGrabber.getTimestamp();
            frame = fFmpegFrameGrabber.grabImage();
            if(count == i){
                if(frame == null){
                    throw  new ConditionalException("无效帧");
                }
                BufferedImage bufferedImage = converter.getBufferedImage(frame);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage,"png",os);
                InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
                //输出黑白剪影文件
                java.io.File outputFile = java.io.File.createTempFile("convert-"+videoId+"-",".png");
                BufferedImage binaryImg = imageUtil.getBodyOutline(bufferedImage, inputStream);
                ImageIO.write(binaryImg,"png",outputFile);
                //有的浏览器或网站需要把图片白色的部分转为透明色，使用以下方法可实现
                imageUtil.transferAlpha(outputFile,outputFile);
                //上传视频剪影文件
                String imgUrl = fastDFSUtil.uploadCommonFile(outputFile,"png");
                VideoBinaryPicture videoBinaryPicture = new VideoBinaryPicture();
                videoBinaryPicture.setFrameNo(i);
                videoBinaryPicture.setUrl(imgUrl);
                videoBinaryPicture.setVideoId(videoId);
                videoBinaryPicture.setVideoTimeStamp(timestamp);
                pictures.add(videoBinaryPicture);
                count += FRAME_NO;
                //删除临时文件
                outputFile.delete();
            }
        }
        //删除临时文件
        java.io.File tmpFile = new java.io.File(filePath);
        tmpFile.delete();
        //批量添加视频剪影文件
        videoDao.batchAddVideoBinaryPictures(pictures);
        return pictures;
    }

    public List<VideoBinaryPicture> getVideoBinaryImages(Map<String, Object> params) {

        return videoDao.getVideoBinaryImages(params);
    }
}
