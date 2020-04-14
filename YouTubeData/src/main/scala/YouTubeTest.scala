import com.google.api.services.youtube.{ YouTube, YouTubeRequestInitializer }
import com.google.api.services.youtube.model.Video
import com.google.api.services.youtube.model.VideoListResponse
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import java.io._
import scala.util.control.Breaks._
import java.util.Calendar
import java.text.SimpleDateFormat

object YouTubeTest {
  def main(args: Array[String]) {
    // init google api access
    val transport = new NetHttpTransport()
    val factory = new JacksonFactory()
    val httpRequestInit = new HttpRequestInitializer {
      override def initialize(re: HttpRequest) = {}
    }

    val Path ="zouxuan/youtube/dataset/"
    val countries: List[String] = List("CA", "DE", "FR", "GB", "IN", "JP", "KR", "MX", "RU", "US")

    for (country <- countries) {

      //val key = "AIzaSyC8MU-69s-iAnrDEQVMDnZU8YpwH1YLRao"
      val key ="AIzaSyDhYg8BlbPwcVrBS_kS-y4FZdyMiii-SPY"
      val service = new YouTube.Builder(transport, factory, httpRequestInit).setApplicationName("YouTubeData").setYouTubeRequestInitializer(new YouTubeRequestInitializer(key)).build()

      // Define and execute the API request
      val videoCatergoryResponse = service.videoCategories().list("snippet").setHl("es").setRegionCode(country).execute()

      val writer = new PrintWriter(Path+country + "_category_id.json", "UTF-8")

      writer.write(videoCatergoryResponse.toString())

      writer.close()

      val header = "video_id,trending_date,title,channel_title,category_id,publish_time,tags,views,likes,dislikes,comment_count,thumbnail_link,comments_disabled,ratings_disabled,video_error_or_removed,description"

      val Videowriter = new PrintWriter(Path+country + "videos.csv", "UTF-8")

      Videowriter.write(header)
      Videowriter.write("\n")

      var nextpagetoken: String = "first"
      breakable {
        for (d <- 0 until 40) {

          var videoResponse: VideoListResponse = null
          if (nextpagetoken == "")
            break

          if (d == 0) {
            videoResponse = service.videos().list("id,statistics,snippet").setChart("mostPopular").setHl("es").setRegionCode(country).execute()
          } else {
            videoResponse = service.videos().list("id,statistics,snippet").setChart("mostPopular").setHl("es").setPageToken(nextpagetoken).setRegionCode(country).execute()
          }

          // Define and execute the API request

          nextpagetoken = videoResponse.getNextPageToken

          val videoList = videoResponse.getItems()

          for (a <- 0 until videoList.size()) {

            val tags = videoList.get(a).getSnippet.getTags
            var tagValue : String = ""
            if (tags != null && tags.size() != 0) {
              for (b <- 0 until tags.size()) {
                 tagValue = tagValue + tags.get(b).replaceAll("\n", "") + "|"
              }
            }

            val viewcount = if (videoList.get(a).getStatistics.getViewCount != null) videoList.get(a).getStatistics.getViewCount.toString() else "0"
            val likeCount = if (videoList.get(a).getStatistics.getLikeCount != null) videoList.get(a).getStatistics.getLikeCount.toString() else "0"
            val dislikecount = if (videoList.get(a).getStatistics.getDislikeCount != null) videoList.get(a).getStatistics.getDislikeCount.toString() else "0"
            val commentcount = if (videoList.get(a).getStatistics.getCommentCount != null) videoList.get(a).getStatistics.getCommentCount.toString() else "0"
            val thumbnail = videoList.get(a).getSnippet.getThumbnails.getDefault.getUrl.toString()
            val comments_disabled = if (commentcount != "0") "False" else "True"
            val ratings_disabled = if (likeCount != "0" || dislikecount != "0") "False" else "True"
            val now = Calendar.getInstance().getTime()
            val minuteFormat = new SimpleDateFormat("dd.MM.yy")
            val currentDateAsString = minuteFormat.format(now)
            val description = videoList.get(a).getSnippet().getDescription.toString().replaceAll("\n", "").replaceAll(",", "")
             
     
            Videowriter.write(videoList.get(a).getId.toString() + "," + currentDateAsString + "," + videoList.get(a).getSnippet().getTitle.toString().replaceAll("\n", "").replaceAll(",", "")
                              + "," + videoList.get(a).getSnippet().getChannelTitle.toString().replaceAll("\n", "").replaceAll(",", "")+ "," + videoList.get(a).getSnippet().getCategoryId.toString()
                              + "," + videoList.get(a).getSnippet().getPublishedAt.toString() + "," + tagValue + "," + viewcount + "," + likeCount + "," + dislikecount + "," + commentcount 
                              + "," + thumbnail + "," + comments_disabled + "," + ratings_disabled
                              + "," + "False"+ "," + description)

            Videowriter.write("\n")
          }
        }
      }
      writer.close()

    }
  }
}
  