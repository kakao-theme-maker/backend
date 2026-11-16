/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
                            `category_id` bigint NOT NULL AUTO_INCREMENT,
                            `owner_id` bigint DEFAULT NULL,
                            `name` varchar(255) NOT NULL,
                            `category_type` enum('BOOKMARK','CUSTOM') NOT NULL,
                            PRIMARY KEY (`category_id`),
                            KEY `FK133xdsbyspb1lxpj7x0fbqcpw` (`owner_id`),
                            CONSTRAINT `FK133xdsbyspb1lxpj7x0fbqcpw` FOREIGN KEY (`owner_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category_post` (
                                 `category_id` bigint DEFAULT NULL,
                                 `category_post_id` bigint NOT NULL AUTO_INCREMENT,
                                 `post_id` bigint DEFAULT NULL,
                                 PRIMARY KEY (`category_post_id`),
                                 UNIQUE KEY `UKmd6e0kqluytmrgyikkf8dmmln` (`category_id`,`post_id`),
                                 KEY `FKmhkf2t5e9lcr9dl9ixc36x4b3` (`post_id`),
                                 CONSTRAINT `FKgrlw4y7oywggm56qbnehfn4a4` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE CASCADE,
                                 CONSTRAINT `FKmhkf2t5e9lcr9dl9ixc36x4b3` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `color_style` (
                               `color_style_id` int NOT NULL AUTO_INCREMENT,
                               `explain` varchar(255) DEFAULT NULL,
                               `name` varchar(255) NOT NULL,
                               `platform_scope` enum('ANDROID','COMMON','IOS') NOT NULL,
                               `style_code` enum('BOTTOM_BANNER_STYLE_BACKGROUND_COLOR','CHAT_ROOM_BACKGROUND_COLOR','DIRECT_SHARE_BAR_BACKGROUND_COLOR','DIRECT_SHARE_BAR_MESSAGE_TEXT_COLOR','FEATURE_STYLE_TEXT_COLOR','HEADER_STYLE_TAB_TEXT_COLOR','HEADER_STYLE_TEXT_COLOR','INPUT_BAR_STYLE_CHAT_BACKGROUND_COLOR','INPUT_BAR_STYLE_CHAT_BUTTON_NORMAL_BACKGROUND_COLOR','INPUT_BAR_STYLE_CHAT_BUTTON_NORMAL_FOREGROUND_COLOR','INPUT_BAR_STYLE_CHAT_BUTTON_TEXT_COLOR','INPUT_BAR_STYLE_CHAT_SEND_NORMAL_BACKGROUND_COLOR','INPUT_BAR_STYLE_CHAT_SEND_NORMAL_FOREGROUND_COLOR','MAINVIEW_STYLE_BACKGROUND_COLOR','MAINVIEW_STYLE_DESCRIPTION_HIGHLIGHTED_TEXT_COLOR','MAINVIEW_STYLE_DESCRIPTION_TEXT_COLOR','MAINVIEW_STYLE_HIGHLIGHTED_TEXT_COLOR','MAINVIEW_STYLE_NORMAL_BACKGROUND_COLOR','MAINVIEW_STYLE_PARAGRAPH_HIGHLIGHTED_TEXT_COLOR','MAINVIEW_STYLE_PARAGRAPH_TEXT_COLOR','MAINVIEW_STYLE_SECONDARY_BACKGROUND_COLOR','MAINVIEW_STYLE_SELECTED_BACKGROUND_COLOR','MAINVIEW_STYLE_TEXT_COLOR','MESSAGE_CELL_STYLE_RECEIVE_TEXT_COLOR','MESSAGE_CELL_STYLE_SEND_TEXT_COLOR','MESSAGE_CELL_STYLE_UNREAD_COUNT_COLOR','MESSAGE_NOTIFICATION_BAR_BACKGROUND_COLOR','MESSAGE_NOTIFICATION_BAR_MESSAGE_TEXT_COLOR','PASSCODE_BACKGROUND_COLOR','PASSCODE_STYLE_KEYPAD_BACKGROUND_COLOR','PASSCODE_STYLE_KEYPAD_TEXT_NORMAL_COLOR','PASSCODE_TITLE_TEXT_COLOR','SECTION_TITLE_STYLE_BORDER_COLOR','SECTION_TITLE_STYLE_TEXT_COLOR','TABBAR_STYLE_BACKGROUND_COLOR') NOT NULL,
                               PRIMARY KEY (`color_style_id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
                           `comment_id` bigint NOT NULL AUTO_INCREMENT,
                           `created_at` datetime(6) DEFAULT NULL,
                           `like_count` bigint NOT NULL,
                           `post_id` bigint DEFAULT NULL,
                           `updated_at` datetime(6) DEFAULT NULL,
                           `user_id` bigint DEFAULT NULL,
                           `content` varchar(255) DEFAULT NULL,
                           PRIMARY KEY (`comment_id`),
                           KEY `FKs1slvnkuemjsq2kj4h3vhx7i1` (`post_id`),
                           KEY `FKn6xssinlrtfnm61lwi0ywn71q` (`user_id`),
                           CONSTRAINT `FKn6xssinlrtfnm61lwi0ywn71q` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
                           CONSTRAINT `FKs1slvnkuemjsq2kj4h3vhx7i1` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=901 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment_like` (
                                `comment_id` bigint NOT NULL,
                                `comment_like_id` bigint NOT NULL AUTO_INCREMENT,
                                `user_id` bigint NOT NULL,
                                PRIMARY KEY (`comment_like_id`),
                                UNIQUE KEY `uk_comment_like_user_comment` (`user_id`,`comment_id`),
                                KEY `idx_comment_like_comment_id` (`comment_id`),
                                CONSTRAINT `FK721ds8ty7bsyej7a3ukfb26gc` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
                                CONSTRAINT `FKqlv8phl1ibeh0efv4dbn3720p` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`comment_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `component_type` (
                                  `component_type_id` int NOT NULL AUTO_INCREMENT,
                                  `created_at` datetime(6) DEFAULT NULL,
                                  `updated_at` datetime(6) DEFAULT NULL,
                                  `explain` varchar(255) DEFAULT NULL,
                                  `name` varchar(255) NOT NULL,
                                  `platform_scope` enum('ANDROID','COMMON','IOS') NOT NULL,
                                  `type_code` enum('BUTTON_STYLE_ADD_FRIEND_IMAGE','CHAT_ROOM_BACKGROUND_IMAGE','COMMON_ICO_THEME','DEFAULT_PROFILE_STYLE_PROFILE_IMAGE','MAINVIEW_STYLE_PRIMARY_BACKGROUND_IMAGE','MESSAGE_CELL_STYLE_RECEIVE_BACKGROUND_IMAGE','MESSAGE_CELL_STYLE_RECEIVE_GROUP_BACKGROUND_IMAGE','MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE','MESSAGE_CELL_STYLE_SEND_GROUP_BACKGROUND_IMAGE','PASSCODE_BACKGROUND_IMAGE','PASSCODE_STYLE_BULLET_FIRST_IMAGE','PASSCODE_STYLE_BULLET_FOURTH_IMAGE','PASSCODE_STYLE_BULLET_SECOND_IMAGE','PASSCODE_STYLE_BULLET_SELECTED_FIRST_IMAGE','PASSCODE_STYLE_BULLET_SELECTED_FOURTH_IMAGE','PASSCODE_STYLE_BULLET_SELECTED_SECOND_IMAGE','PASSCODE_STYLE_BULLET_SELECTED_THIRD_IMAGE','PASSCODE_STYLE_BULLET_THIRD_IMAGE','TABBAR_STYLE_BACKGROUND_IMAGE','TABBAR_STYLE_CHATS_NORMAL_ICON_IMAGE','TABBAR_STYLE_CHATS_SELECTED_ICON_IMAGE','TABBAR_STYLE_FRIENDS_NORMAL_ICON_IMAGE','TABBAR_STYLE_FRIENDS_SELECTED_ICON_IMAGE','TABBAR_STYLE_MORE_NORMAL_ICON_IMAGE','TABBAR_STYLE_MORE_SELECTED_ICON_IMAGE','TABBAR_STYLE_OPEN_CHATS_NORMAL_ICON_IMAGE','TABBAR_STYLE_OPEN_CHATS_SELECTED_ICON_IMAGE','TABBAR_STYLE_SHOPPING_NORMAL_ICON_IMAGE','TABBAR_STYLE_SHOPPING_SELECTED_ICON_IMAGE') NOT NULL,
                                  PRIMARY KEY (`component_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `design_board` (
                                `design_component_id` int NOT NULL,
                                `design_board_id` bigint NOT NULL AUTO_INCREMENT,
                                `post_id` bigint NOT NULL,
                                PRIMARY KEY (`design_board_id`),
                                UNIQUE KEY `unique_design_board` (`post_id`,`design_component_id`),
                                KEY `FKskiyfk40caxga2ntjemsv2h82` (`design_component_id`),
                                CONSTRAINT `FK1r455cr27gnlr7kvbjqi90a3u` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`),
                                CONSTRAINT `FKskiyfk40caxga2ntjemsv2h82` FOREIGN KEY (`design_component_id`) REFERENCES `design_component` (`design_component_id`)
) ENGINE=InnoDB AUTO_INCREMENT=91 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `design_component` (
                                    `design_component_id` int NOT NULL AUTO_INCREMENT,
                                    `is_public` bit(1) DEFAULT NULL,
                                    `created_at` datetime(6) DEFAULT NULL,
                                    `updated_at` datetime(6) DEFAULT NULL,
                                    `user_id` bigint DEFAULT NULL,
                                    `file_name` varchar(255) DEFAULT NULL,
                                    `is_active` bit(1) NOT NULL,
                                    PRIMARY KEY (`design_component_id`),
                                    KEY `FKb6nf213ccwqhbo72ojygi31m6` (`user_id`),
                                    CONSTRAINT `FKb6nf213ccwqhbo72ojygi31m6` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=193 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `design_component_component_type` (
                                                   `component_type_id` int NOT NULL,
                                                   `design_component_component_type_id` int NOT NULL AUTO_INCREMENT,
                                                   `design_component_id` int NOT NULL,
                                                   PRIMARY KEY (`design_component_component_type_id`),
                                                   UNIQUE KEY `uk_design_component_component_type` (`design_component_id`,`component_type_id`),
                                                   KEY `FKdkp25b8jt6luggnyrocgkmm6p` (`component_type_id`),
                                                   CONSTRAINT `FKc4cjfq63be24tvhje2n2b588c` FOREIGN KEY (`design_component_id`) REFERENCES `design_component` (`design_component_id`),
                                                   CONSTRAINT `FKdkp25b8jt6luggnyrocgkmm6p` FOREIGN KEY (`component_type_id`) REFERENCES `component_type` (`component_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=199 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `platform_color_style` (
                                        `color_style_id` int DEFAULT NULL,
                                        `platform` tinyint NOT NULL,
                                        `weight` double NOT NULL,
                                        `platform_color_style_id` bigint NOT NULL AUTO_INCREMENT,
                                        `code` varchar(255) NOT NULL,
                                        `resource_group` varchar(255) NOT NULL,
                                        `resource_name` varchar(255) NOT NULL,
                                        PRIMARY KEY (`platform_color_style_id`),
                                        UNIQUE KEY `UKnp5xo52n1sv8h4w96q5aayg1d` (`code`),
                                        KEY `FKq24i852kyua0n6bbkyhqvv511` (`color_style_id`),
                                        CONSTRAINT `FKq24i852kyua0n6bbkyhqvv511` FOREIGN KEY (`color_style_id`) REFERENCES `color_style` (`color_style_id`),
                                        CONSTRAINT `platform_color_style_chk_1` CHECK ((`platform` between 0 and 1))
) ENGINE=InnoDB AUTO_INCREMENT=87 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `platform_component_type` (
                                           `component_type_id` int DEFAULT NULL,
                                           `height` int NOT NULL,
                                           `width` int NOT NULL,
                                           `platform_component_type_id` bigint NOT NULL AUTO_INCREMENT,
                                           `code` varchar(255) NOT NULL,
                                           `path` varchar(255) NOT NULL,
                                           `file_extension` enum('NINE_PATCH_PNG','PNG') NOT NULL,
                                           `platform` enum('ANDROID','IOS') NOT NULL,
                                           PRIMARY KEY (`platform_component_type_id`),
                                           UNIQUE KEY `UKa3s9tarav943bsqddle6awb0s` (`code`),
                                           KEY `FKnnxjioulnyeu98csqeabq243u` (`component_type_id`),
                                           CONSTRAINT `FKnnxjioulnyeu98csqeabq243u` FOREIGN KEY (`component_type_id`) REFERENCES `component_type` (`component_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=98 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post` (
                        `created_at` datetime(6) DEFAULT NULL,
                        `post_id` bigint NOT NULL AUTO_INCREMENT,
                        `updated_at` datetime(6) DEFAULT NULL,
                        `user_id` bigint DEFAULT NULL,
                        `content` text,
                        `preview_image_name` varchar(255) DEFAULT NULL,
                        `title` varchar(255) DEFAULT NULL,
                        `post_type` enum('DESIGN_BOARD','THEME_BOARD') NOT NULL,
                        PRIMARY KEY (`post_id`),
                        KEY `FK51aeb5le008k8klgnyfaalmn` (`user_id`),
                        CONSTRAINT `FK51aeb5le008k8klgnyfaalmn` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=181 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prefer` (
                          `post_id` bigint DEFAULT NULL,
                          `prefer_id` bigint NOT NULL AUTO_INCREMENT,
                          `user_id` bigint DEFAULT NULL,
                          PRIMARY KEY (`prefer_id`),
                          KEY `FKbayv45seqehliuti75iir90je` (`post_id`),
                          KEY `FK9dwonefxoibuavuftxyrc6ssm` (`user_id`),
                          CONSTRAINT `FK9dwonefxoibuavuftxyrc6ssm` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
                          CONSTRAINT `FKbayv45seqehliuti75iir90je` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=455 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tag` (
                       `post_id` bigint DEFAULT NULL,
                       `tag_id` bigint NOT NULL AUTO_INCREMENT,
                       `tag_name` varchar(255) DEFAULT NULL,
                       PRIMARY KEY (`tag_id`),
                       KEY `FK7tk5hi5tl1txftyn44dtq2mv6` (`post_id`),
                       CONSTRAINT `FK7tk5hi5tl1txftyn44dtq2mv6` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `theme_board` (
                               `theme_component_id` int NOT NULL,
                               `post_id` bigint NOT NULL,
                               `theme_board_id` bigint NOT NULL AUTO_INCREMENT,
                               PRIMARY KEY (`theme_board_id`),
                               UNIQUE KEY `UKsvh6ew84e4ikt3pveio61ljlu` (`theme_component_id`),
                               UNIQUE KEY `UKhueaoepxemo40su79q6bfqpkk` (`post_id`),
                               CONSTRAINT `FKcv1j27pwsbhl5ps67721v68y8` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`),
                               CONSTRAINT `FKryxhdjw1r2j4ibk5vfq6bxvg6` FOREIGN KEY (`theme_component_id`) REFERENCES `theme_component` (`theme_component_id`)
) ENGINE=InnoDB AUTO_INCREMENT=91 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `theme_build_job` (
                                   `theme_component_id` int NOT NULL,
                                   `build_id` bigint NOT NULL AUTO_INCREMENT,
                                   `created_at` datetime(6) NOT NULL,
                                   `updated_at` datetime(6) NOT NULL,
                                   `file_name` varchar(255) DEFAULT NULL,
                                   `platform` enum('ANDROID','IOS') NOT NULL,
                                   `status` enum('FAILED','RUNNING','SUCCESS') NOT NULL,
                                   PRIMARY KEY (`build_id`),
                                   KEY `idx_theme_build_job_theme_platform_status_created` (`theme_component_id`,`platform`,`status`,`created_at`),
                                   CONSTRAINT `FK5e18xgk0sujolmho9icfhsq20` FOREIGN KEY (`theme_component_id`) REFERENCES `theme_component` (`theme_component_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `theme_component` (
                                   `is_done` bit(1) DEFAULT NULL,
                                   `is_public` bit(1) DEFAULT NULL,
                                   `theme_component_id` int NOT NULL AUTO_INCREMENT,
                                   `created_at` datetime(6) DEFAULT NULL,
                                   `updated_at` datetime(6) DEFAULT NULL,
                                   `user_id` bigint NOT NULL,
                                   `theme_code` varchar(255) NOT NULL,
                                   `theme_name` varchar(255) NOT NULL,
                                   `version_name` varchar(255) DEFAULT NULL,
                                   `version_number` varchar(255) NOT NULL,
                                   `theme_type` enum('DEFAULT','USER') NOT NULL,
                                   PRIMARY KEY (`theme_component_id`),
                                   UNIQUE KEY `UKhpshjhq31bh70jwi7vxg3e7te` (`theme_code`),
                                   KEY `FKhngpxsscre8btjoyshj2ymnro` (`user_id`),
                                   CONSTRAINT `FKhngpxsscre8btjoyshj2ymnro` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=183 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `theme_image` (
                               `component_type_id` int DEFAULT NULL,
                               `design_component_id` int DEFAULT NULL,
                               `edge_inset_bottom` int DEFAULT NULL,
                               `edge_inset_left` int DEFAULT NULL,
                               `edge_inset_right` int DEFAULT NULL,
                               `edge_inset_top` int DEFAULT NULL,
                               `stretchx` int DEFAULT NULL,
                               `stretchy` int DEFAULT NULL,
                               `theme_component_id` int DEFAULT NULL,
                               `theme_image_id` bigint NOT NULL AUTO_INCREMENT,
                               PRIMARY KEY (`theme_image_id`),
                               UNIQUE KEY `THEME_COMPONENT_COMPONENT_TYPE_UNIQUE` (`theme_component_id`,`component_type_id`),
                               KEY `FKkm224en3wxvr9vk393q353ji` (`component_type_id`),
                               KEY `FKjyxe8nflqfrsm2hnndxhjw0x7` (`design_component_id`),
                               CONSTRAINT `FKjyxe8nflqfrsm2hnndxhjw0x7` FOREIGN KEY (`design_component_id`) REFERENCES `design_component` (`design_component_id`),
                               CONSTRAINT `FKkm224en3wxvr9vk393q353ji` FOREIGN KEY (`component_type_id`) REFERENCES `component_type` (`component_type_id`),
                               CONSTRAINT `FKqoek74qvpiftmm6y67eyda92d` FOREIGN KEY (`theme_component_id`) REFERENCES `theme_component` (`theme_component_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4003 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `theme_style` (
                               `alpha` int NOT NULL,
                               `color_style_id` int DEFAULT NULL,
                               `theme_component_id` int DEFAULT NULL,
                               `theme_style_id` bigint NOT NULL AUTO_INCREMENT,
                               `color` varchar(255) DEFAULT NULL,
                               PRIMARY KEY (`theme_style_id`),
                               UNIQUE KEY `THEME_COMPONENT_COLOR_STYLE_UNIQUE` (`theme_component_id`,`color_style_id`),
                               KEY `FKo2tleuefo11hmpb1skefgjoqa` (`color_style_id`),
                               CONSTRAINT `FK5fctj77b105sywf065d37rf6c` FOREIGN KEY (`theme_component_id`) REFERENCES `theme_component` (`theme_component_id`),
                               CONSTRAINT `FKo2tleuefo11hmpb1skefgjoqa` FOREIGN KEY (`color_style_id`) REFERENCES `color_style` (`color_style_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4831 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
                        `birth` date DEFAULT NULL,
                        `gender` tinyint DEFAULT NULL,
                        `created_at` datetime(6) DEFAULT NULL,
                        `updated_at` datetime(6) DEFAULT NULL,
                        `user_id` bigint NOT NULL AUTO_INCREMENT,
                        `encrypted_password` varchar(255) DEFAULT NULL,
                        `introduce` varchar(255) DEFAULT NULL,
                        `name` varchar(255) DEFAULT NULL,
                        `profile_img_name` varchar(255) DEFAULT NULL,
                        `profile_img_url` varchar(255) DEFAULT NULL,
                        `public_user_id` varchar(255) NOT NULL,
                        `user_email` varchar(255) NOT NULL,
                        `auth_provider` varchar(20) NOT NULL DEFAULT 'LOCAL',
                        `role` varchar(20) NOT NULL DEFAULT 'USER',
                        PRIMARY KEY (`user_id`),
                        UNIQUE KEY `UKlucoih70m72p36l3kp875ap9d` (`public_user_id`),
                        UNIQUE KEY `UKr26f0rbwwh96866v2wixmj64w` (`user_email`),
                        CONSTRAINT `user_chk_1` CHECK ((`gender` between 0 and 1))
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_follow` (
                               `follow_id` bigint NOT NULL AUTO_INCREMENT,
                               `followed_at` datetime(6) DEFAULT NULL,
                               `followee_id` bigint NOT NULL,
                               `follower_id` bigint NOT NULL,
                               PRIMARY KEY (`follow_id`),
                               UNIQUE KEY `uk_user_follow_follower_followee` (`follower_id`,`followee_id`),
                               KEY `FK65xv8l8kxo1wv6xkjnk5aemfx` (`followee_id`),
                               CONSTRAINT `FK2whm07sllkjj58a67b98y5cvc` FOREIGN KEY (`follower_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
                               CONSTRAINT `FK65xv8l8kxo1wv6xkjnk5aemfx` FOREIGN KEY (`followee_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
