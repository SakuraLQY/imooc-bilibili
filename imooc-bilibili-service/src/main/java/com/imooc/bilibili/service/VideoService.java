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
     * ??????????????????
     * @param size
     * @param no
     * @param area
     * @return
     */
    public PageResult<Video>pageListVideo(Integer size,Integer no,String area){
        if(size ==null || no == null){
            throw new ConditionalException("????????????");
        }
        Map<String,Object>params = new HashMap<>();
        params.put("start",(no-1)*size);
        params.put("limit",size);
        params.put("area",area);
        List<Video>list = new ArrayList<>();//???????????????????????????
        Integer total = videoDao.pageCountVideos(params);
        if(total>0){
            list = videoDao.pageListVideos(params);
        }
        return new PageResult<>(total,list);
    }

    /**
     * ????????????????????????
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
            throw new ConditionalException("???????????????");
        }
        Long tagId = videoTag.getTagId();
        Tag tagInfo = new Tag();
        tagInfo.setId(tagId);
        tagInfo.setName(videoTag.getTag().getName());
        tagInfo.setCreateTime(new Date());
        videoDao.addVideoTags(tagInfo);
    }


    public List<Tag> getVideoTags(Long videoId){
        //????????????????????????tagId
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

    //??????
    public void addVideoLike(Long videoId, Long userId) {
        Video video =  videoDao.getVideoById(videoId);
        if(video==null){
            throw new ConditionalException("????????????");
        }
        VideoLike videoLike =  videoDao.getVideoLikeByVideoIdAndUserId(videoId,userId);
        if(videoLike!=null){
            throw new ConditionalException("???????????????");
        }
        videoLike = new VideoLike();
        videoLike.setVideoId(videoId);
        videoLike.setUserId(userId);
        videoLike.setCreateTime(new Date());
        videoDao.addVideoLike(videoLike);
    }

    //????????????
    public void deleteVideoLike(Long videoId, Long userId) {
        videoDao.deleteVideoLike(videoId,userId);
    }

    //???????????????
    public Map<String, Object> getVideoLike(Long videoId, Long userId) {
        //Map???????????????like count
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
            throw  new ConditionalException("????????????");
        }
        //?????????video?????????
        Video video = videoDao.getVideoById(videoId);
        if(video==null){
            throw new ConditionalException("????????????");
        }
        //????????????????????????
        videoDao.deleteVideoCollection(videoId, userId);
        //????????????????????????
        videoCollection.setUserId(userId);
        videoCollection.setCreateTime(new Date());
        videoDao.addVideoCollection(videoCollection);
    }

    public void deleteVideoCollection(Long videoId, Long userId) {
        videoDao.deleteVideoCollection(videoId,userId);
    }

    //??????????????????
    public Map<String, Object> getVideoCollections(Long videoId, Long userId) {
        Long count = videoDao.getVideoCollections(videoId);
        //??????????????????
        VideoCollection videoCollection = videoDao.getVideoCollectionByVideoIdAndUserId(videoId, userId);
        boolean like = videoCollection != null;
        Map<String, Object> result = new HashMap<>();
        result.put("count",count);
        result.put("like",like);
        return result;
    }

    @Transactional
    public void addVideoCoins(VideoCoin videoCoin, Long userId) {
        Integer amount = videoCoin.getAmount();//??????????????????
        Long videoId = videoCoin.getVideoId();
        if(userId==null || videoId==null){
            throw new ConditionalException("????????????");
        }
        Video video = videoDao.getVideoById(videoId);
        if(video==null){
            throw new ConditionalException("????????????");
        }
        //??????????????????????????????
        Integer userCoinAmount =  userCoinService.getUserCoinAmount(userId);
        userCoinAmount = userCoinAmount==null? 0:userCoinAmount;
        if(amount > userCoinAmount){
            throw new ConditionalException("????????????????????????");
        }
        //????????????????????????????????????????????????????????????
        //??????????????????????????????????????????
        VideoCoin dbVideoCoin = videoDao.getVideoCoinByVideoIdAndUserId(videoId, userId);
        //??????????????????
        if(dbVideoCoin==null){
            videoCoin.setUserId(userId);
            videoCoin.setCreateTime(new Date());
            videoDao.addVideoCoin(videoCoin);
        }else{
            Integer dbAmount = dbVideoCoin.getAmount();//???????????????
            dbAmount+=amount;
            videoCoin.setUserId(userId);
            videoCoin.setAmount(dbAmount);
            videoCoin.setUpdateTime(new Date());
            videoDao.updateVideoCoin(videoCoin);
        }
        userCoinService.updateUserCoinsAmount(userId,(userCoinAmount-amount));
    }

    //??????????????????
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
            throw new ConditionalException("????????????!");
        }
        Video video = videoDao.getVideoById(videoId);
        if(video==null){
            throw new ConditionalException("????????????!");
        }
        videoComment.setUserId(userId);
        videoComment.setCreateTime(new Date());
        videoDao.addVideoComment(videoComment);
    }

    /**
     * ????????????
     * @param size
     * @param no
     * @param videoId
     * @return
     */
    public PageResult<VideoComment> pageListVideoComments(Integer size, Integer no, Long videoId) {
        Video video = videoDao.getVideoById(videoId);
        if(video==null){
            throw new ConditionalException("????????????!");
        }
        Map<String,Object>params = new HashMap<>();
        params.put("start",(no-1)*size);
        params.put("limit",size);
        params.put("videoId",videoId);
        //??????????????????
        Integer total =  videoDao.pageCountVideoComments(params);
        List<VideoComment>list = new ArrayList<>();
        if(total > 0){
            //1.????????????????????????
            list = videoDao.pageListVideoComments(params);
            //2.????????????????????????
            List<Long>parentIdList = list.stream().map(VideoComment::getId).collect(Collectors.toList());
            List<VideoComment>childCommentList = videoDao.batchGetVideoCommentsByRootIds(parentIdList);//??????id->???????????????rootIdList??????
            //????????????????????????
            Set<Long> userIdList = list.stream().map(VideoComment::getUserId).collect(Collectors.toSet());
//            Set<Long> replyUserId = childCommentList.stream().map(VideoComment::getUserId).collect(Collectors.toSet());
            Set<Long> replyUserIdList = childCommentList.stream().map(VideoComment::getReplyUserId).collect(Collectors.toSet());
            userIdList.addAll(replyUserIdList);
//            userIdList.addAll(childUserIdList);
            //????????????????????????userId???????????????????????????
            List<UserInfo>userInfoList = userService.batchGetUserInfoByUserIds(userIdList);
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getUserId, userInfo -> userInfo));
            //?????????????????????????????????????????????
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
     * ??????????????????
     * @param videoId
     * @return
     */
    public Map<String, Object> getVideoDetails(Long videoId) {
        Video video = videoDao.getVideoDetails(videoId);
        Long userId = video.getUserId();
        if(video==null){
            throw new ConditionalException("????????????");
        }
        //??????????????????????????????
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
        //??????clientId
        String agent = request.getHeader("User-Agent");
        UserAgent userAgent = UserAgent.parseUserAgentString(agent);
        String clientId = String.valueOf(userAgent.getId());
        String ip = IpUtil.getIP(request);
        Map<String,Object>params = new HashMap<>();
        if(userId != null){//????????????
            params.put("userId",userId);
        }else{
            params.put("ip",ip);
            params.put("clientId",clientId);
        }
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        params.put("today",sdf.format(now));
        params.put("videoId",videoId);
        //??????????????????
        VideoView dbVideoView = videoDao.getVideoView(params);
        //???????????????????????????????????????
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

    //??????????????????????????????????????????
    public List<Video> recommend(Long userId) throws TasteException {
        List<UserPreference>list = videoDao.getAllUserPreference();
        //??????????????????
        DataModel dataModel = this.createDataModel(list);
        //2.??????similar?????????
        UserSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
        //3.????????????userNeighborhood
        System.out.println(similarity.userSimilarity(7,8));
        UserNeighborhood userNeighborhood = new NearestNUserNeighborhood(2,similarity,dataModel);
        long[]ar = userNeighborhood.getUserNeighborhood(userId);
        //4.???????????????recommend
        Recommender recommender = new GenericUserBasedRecommender(dataModel,userNeighborhood,similarity);
        List<RecommendedItem>recommendedItems = recommender.recommend(userId,5);//???????????????5?????????
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
        fFmpegFrameGrabber.start();//??????
        int ffLength = fFmpegFrameGrabber.getLengthInFrames();
        //?????????
        Frame frame;
        Java2DFrameConverter converter = new Java2DFrameConverter();
        int count = 1;
        List<VideoBinaryPicture>pictures = new ArrayList<>();
        for(int i =1;i<=ffLength;i++){
            Long timestamp = fFmpegFrameGrabber.getTimestamp();
            frame = fFmpegFrameGrabber.grabImage();
            if(count == i){
                if(frame == null){
                    throw  new ConditionalException("?????????");
                }
                BufferedImage bufferedImage = converter.getBufferedImage(frame);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage,"png",os);
                InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
                //????????????????????????
                java.io.File outputFile = java.io.File.createTempFile("convert-"+videoId+"-",".png");
                BufferedImage binaryImg = imageUtil.getBodyOutline(bufferedImage, inputStream);
                ImageIO.write(binaryImg,"png",outputFile);
                //???????????????????????????????????????????????????????????????????????????????????????????????????
                imageUtil.transferAlpha(outputFile,outputFile);
                //????????????????????????
                String imgUrl = fastDFSUtil.uploadCommonFile(outputFile,"png");
                VideoBinaryPicture videoBinaryPicture = new VideoBinaryPicture();
                videoBinaryPicture.setFrameNo(i);
                videoBinaryPicture.setUrl(imgUrl);
                videoBinaryPicture.setVideoId(videoId);
                videoBinaryPicture.setVideoTimeStamp(timestamp);
                pictures.add(videoBinaryPicture);
                count += FRAME_NO;
                //??????????????????
                outputFile.delete();
            }
        }
        //??????????????????
        java.io.File tmpFile = new java.io.File(filePath);
        tmpFile.delete();
        //??????????????????????????????
        videoDao.batchAddVideoBinaryPictures(pictures);
        return pictures;
    }

    public List<VideoBinaryPicture> getVideoBinaryImages(Map<String, Object> params) {

        return videoDao.getVideoBinaryImages(params);
    }
}
