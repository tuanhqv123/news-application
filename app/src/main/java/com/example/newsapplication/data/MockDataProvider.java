package com.example.newsapplication.data;

import com.example.newsapplication.R;
import com.example.newsapplication.model.Article;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MockDataProvider {

    private static final Random random = new Random();

    public static List<Article> getBreakingNews() {
        List<Article> articles = new ArrayList<>();

        String[] titles = {
            "Công nghệ AI thay đổi ngành y tế Việt Nam",
            "Thị trường chứng khoán tăng điểm mạnh trong phiên sáng",
            "NASA phát hiện hành tinh mới có thể sống được",
            "Đội tuyển Việt Nam chuẩn bị cho trận đấu quan trọng",
            "Giá xăng dầu dự kiến sẽ giảm trong tuần tới"
        };

        String[] descriptions = {
            "Các chuyên gia y tế đánh giá cao tiềm năng của trí tuệ nhân tạo trong việc chẩn đoán và điều trị bệnh.",
            "Nhờ dòng tiền mạnh từ các tổ chức đầu tư, VN-Index đã vượt mốc quan trọng.",
            "Hành tinh mới nằm trong khu vực có thể tồn tại sự sống và có điều kiện tương tự Trái Đất.",
            "HLV Park Hang-seo đang tích cực chuẩn bị tinh thần và thể lực cho các cầu thủ.",
            "Thế giới giá xăng dầu đang có xu hướng giảm, ảnh hưởng tích cực đến thị trường nội địa."
        };

        String[] sources = {"VnExpress", "Tuổi Trẻ", "Thanh Niên", "Zing News", "Công an Nhân dân"};
        String[] categories = {"Công nghệ", "Kinh tế", "Thế giới", "Thể thao", "Xã hội"};
        int[] imageResIds = {
            R.drawable.ic_launcher_foreground,
            R.drawable.placeholder_image,
            R.drawable.ic_home_filled,
            R.drawable.ic_explore_filled,
            R.drawable.ic_bookmark_filled
        };

        for (int i = 0; i < titles.length; i++) {
            Article article = new Article(
                "breaking_" + i,
                titles[i],
                descriptions[i],
                generateFullContent(descriptions[i]),
                sources[random.nextInt(sources.length)],
                sources[random.nextInt(sources.length)],
                categories[i % categories.length],
                "https://picsum.photos/400/300?random=" + i, // Mock image URL
                imageResIds[i],
                "17/11/2025",
                i % 3 == 0 // Make every 3rd article a video
            );
            articles.add(article);
        }

        return articles;
    }

    public static List<Article> getPopularNews() {
        List<Article> articles = new ArrayList<>();

        String[] titles = {
            "Hội nghị COP28 thảo luận về biến đổi khí hậu",
            "Apple ra mắt iPhone mới với tính năng đột phá",
            "Giá bất động sản Hà Nội có xu hướng giảm",
            "Báo cáo kinh tế quý 4 của Việt Nam",
            "Khám phá vũ trụ: Kế hoạch sao Hỏa 2025",
            "Du lịch Việt Nam đón 5 triệu khách quốc tế",
            "Chương trình chuyển đổi số quốc gia",
            "Nông nghiệp công nghệ cao phát triển mạnh mẽ",
            "Đầu tư hạ tầng giao thông các tỉnh miền Tây",
            "Giáo dục Việt Nam trên bảng xếp hạng quốc tế"
        };

        String[] descriptions = {
            "Các nhà lãnh đạo thế giới cùng nhau tìm giải pháp cho vấn đề nóng của toàn cầu.",
            "Phiên bản mới được trang bị chip A17 Pro với hiệu năng vượt trội và camera được cải tiến.",
            "Thị trường đang điều chỉnh sau giai đoạn tăng nóng, nhiều cơ hội cho người mua nhà.",
            "Tăng trưởng GDP đạt 6.8%, vượt kỳ vọng của các chuyên gia kinh tế.",
            "NASA và SpaceX hợp tác trong sứ mệnh lịch sử đưa người lên sao Hỏa.",
            "Ngành du lịch phục hồi mạnh mẽ sau đại dịch, thu hút du khách quốc tế.",
            "Chính phủ đẩy mạnh ứng dụng công nghệ trong quản lý và dịch vụ công.",
            "Các mô hình nông nghiệp thông minh giúp tăng năng suất và giảm chi phí.",
            "Dự án cao tốc và cầu nối các tỉnh đồng bằng sông Cửu Long sắp hoàn thành.",
            "Hệ thống giáo dục Việt Nam vươn lên mạnh mẽ trên trường quốc tế."
        };

        String[] sources = {"VnExpress", "Tuổi Trẻ", "Thanh Niên", "Zing News", "Công an Nhân dân"};
        String[] categories = {"Thế giới", "Công nghệ", "Bất động sản", "Kinh tế", "Khoa học", "Du lịch", "Xã hội", "Nông nghiệp", "Giao thông", "Giáo dục"};
        int[] imageResIds = {
            R.drawable.ic_launcher_foreground,
            R.drawable.placeholder_image,
            R.drawable.ic_home_filled,
            R.drawable.ic_explore_filled,
            R.drawable.ic_bookmark_filled,
            R.drawable.ic_home_outline,
            R.drawable.ic_explore_outline,
            R.drawable.ic_profile_outline,
            R.drawable.ic_bookmark_outline,
            R.drawable.error_image
        };

        for (int i = 0; i < titles.length; i++) {
            Article article = new Article(
                "popular_" + i,
                titles[i],
                descriptions[i],
                generateFullContent(descriptions[i]),
                sources[random.nextInt(sources.length)],
                sources[random.nextInt(sources.length)],
                categories[i],
                "https://picsum.photos/400/300?random=" + i, // Mock image URL
                imageResIds[i],
                "16/11/2025",
                false
            );
            articles.add(article);
        }

        return articles;
    }

    public static List<Article> getCategoryNews(String category) {
        List<Article> articles = new ArrayList<>();

        switch (category) {
            case "Thời sự":
                articles.addAll(getPoliticalNews());
                break;
            case "Kinh doanh":
                articles.addAll(getBusinessNews());
                break;
            case "Công nghệ":
                articles.addAll(getTechNews());
                break;
            case "Thể thao":
                articles.addAll(getSportsNews());
                break;
            default:
                articles.addAll(getPopularNews());
        }

        return articles;
    }

    private static List<Article> getPoliticalNews() {
        List<Article> articles = new ArrayList<>();
        String[] titles = {
            "Quốc hội thảo luận Luật Đất đàisửa đổi",
            "Chính phủ ban hành chính sách mới về phát triển bền vững",
            "Hội nghị cấp cao ASEAN diễn ra tại Hà Nội"
        };

        for (int i = 0; i < titles.length; i++) {
            Article article = new Article(
                "political_" + i,
                titles[i],
                "Nội dung chi tiết sẽ được cập nhật trong phiên làm việc tới.",
                generateFullContent("Nội dung chi tiết sẽ được cập nhật trong phiên làm việc tới."),
                "VnExpress",
                "VnExpress",
                "Thời sự",
                "https://picsum.photos/400/300?random=" + i, // Mock image URL
                R.drawable.ic_dashboard_black_24dp,
                "17/11/2025",
                i % 3 == 0 // Make every 3rd article a video
            );
            articles.add(article);
        }

        return articles;
    }

    private static List<Article> getBusinessNews() {
        List<Article> articles = new ArrayList<>();
        String[] titles = {
            "Ngân hàng trung ương điều chỉnh lãi suất",
            "Hàng không Việt Nam phục hồi sau đại dịch",
            "Doanh nghiệp FDI tiếp tục rót vốn vào Việt Nam"
        };

        for (int i = 0; i < titles.length; i++) {
            Article article = new Article(
                "business_" + i,
                titles[i],
                "Thị trường đang có những chuyển biến tích cực.",
                generateFullContent("Thị trường đang có những chuyển biến tích cực."),
                "Tuổi Trẻ",
                "Tuổi Trẻ",
                "Kinh doanh",
                "https://picsum.photos/400/300?random=" + i, // Mock image URL
                R.drawable.ic_notifications_black_24dp,
                "17/11/2025",
                i % 3 == 0 // Make every 3rd article a video
            );
            articles.add(article);
        }

        return articles;
    }

    private static List<Article> getTechNews() {
        List<Article> articles = new ArrayList<>();
        String[] titles = {
            "Google ra mắt mẫu Google Glass mới",
            "Meta công bố VR headset thế hệ mới",
            "Tesla giới thiệu tính năng tự lái hoàn toàn"
        };

        for (int i = 0; i < titles.length; i++) {
            Article article = new Article(
                "tech_" + i,
                titles[i],
                "Công nghệ mới hứa hẹn thay đổi cuộc sống của chúng ta.",
                generateFullContent("Công nghệ mới hứa hẹn thay đổi cuộc sống của chúng ta."),
                "Zing News",
                "Zing News",
                "Công nghệ",
                "https://picsum.photos/400/300?random=" + i, // Mock image URL
                R.drawable.ic_home_black_24dp,
                "17/11/2025",
                i % 3 == 0 // Make every 3rd article a video
            );
            articles.add(article);
        }

        return articles;
    }

    private static List<Article> getSportsNews() {
        List<Article> articles = new ArrayList<>();
        String[] titles = {
            "Manchester United thắng đậm trong trận derby",
            "Tiger Woods trở lại thi đấu sau chấn thương",
            "Việt Nam giành huy chương vàng SEA Games"
        };

        for (int i = 0; i < titles.length; i++) {
            Article article = new Article(
                "sports_" + i,
                titles[i],
                "Một trận đấu đầy kịch tính và cảm xúc.",
                generateFullContent("Một trận đấu đầy kịch tính và cảm xúc."),
                "Thanh Niên",
                "Thanh Niên",
                "Thể thao",
                "https://picsum.photos/400/300?random=" + i, // Mock image URL
                R.drawable.ic_play_circle,
                "17/11/2025",
                i % 3 == 0 // Make every 3rd article a video
            );
            articles.add(article);
        }

        return articles;
    }

    private static String generateFullContent(String description) {
        return description + "\n\n" +
                "Đây là nội dung đầy đủ của bài viết. Trong phiên bản thực tế, nội dung này sẽ chứa thông tin chi tiết, phân tích sâu và các câu chuyện liên quan. Bài viết sẽ cung cấp cho người đọc cái nhìn toàn diện về chủ đề đang được trình bày.\n\n" +
                "Nội dung bao gồm các thông tin chính, số liệu thống kê, ý kiến chuyên gia và dự báo xu hướng. Người đọc sẽ có được kiến thức nền tảng và hiểu rõ hơn về vấn đề đang được đề cập.";
    }
}
