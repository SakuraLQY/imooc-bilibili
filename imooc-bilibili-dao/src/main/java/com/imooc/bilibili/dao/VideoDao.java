package com.imooc.bilibili.dao;

import com.imooc.bilibili.domain.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface VideoDao {
    Integer addVideos(Video video);

    Integer batchAddVideoTags(List<VideoTag> tagList);

    Integer pageCountVideos(Map<String, Object> params);

    List<Video> pageListVideos(Map<String, Object> params);

    Integer addVideoTags(Tag tagInfo);

    List<VideoTag> getVideoTagList(Long videoId);

    List<Tag> getTagInfoList(Set<Long> tagList);

    Integer deleteVideoTags(@Param("tagList") List<Long> tagList
            , @Param("videoId") Long videoId);

    Video getVideoById(Long videoId);

    VideoLike getVideoLikeByVideoIdAndUserId(@Param("videoId")Long videoId, @Param("userId") Long userId);

    Integer addVideoLike(VideoLike videoLike);

    Integer deleteVideoLike(@Param("videoId") Long videoId
                            ,@Param("userId") Long userId);

    Long getVideoLikes(Long videoId);

    Integer deleteVideoCollection(@Param("videoId") Long videoId,@Param("userId") Long userId);

    Integer addVideoCollection(VideoCollection videoCollection);

    Long getVideoCollections(Long videoId);

    VideoCollection getVideoCollectionByVideoIdAndUserId(Long videoId, Long userId);

    VideoCoin getVideoCoinByVideoIdAndUserId(@Param("videoId") Long videoId
                                              ,@Param("userId") Long userId);

    Integer addVideoCoin(VideoCoin videoCoin);

    Integer updateVideoCoin(VideoCoin videoCoin);

    Long getVideoCoinsAmount(Long videoId);

    Integer addVideoComment(VideoComment videoComment);

    Integer pageCountVideoComments(Map<String, Object> params);

    List<VideoComment> pageListVideoComments(Map<String, Object> params);

    List<VideoComment> batchGetVideoCommentsByRootIds(@Param("rootIdList") List<Long> rootIdList);

    Video getVideoDetails(Long id);

    VideoView getVideoView(Map<String, Object> params);

    Integer addVideoView(VideoView videoView);

    Integer getVideoViewCount(Long videoId);

    List<UserPreference> getAllUserPreference();

    List<Video> batchGetVideoByIds(@Param("idList") List<Long> idList);

    Integer batchAddVideoBinaryPictures(@Param("pictureList") List<VideoBinaryPicture> pictureList);

    List<VideoBinaryPicture> getVideoBinaryImages(Map<String, Object> params);
}
