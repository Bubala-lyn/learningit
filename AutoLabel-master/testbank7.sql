/*
 Navicat Premium Data Transfer

 Source Server         : autolabel
 Source Server Type    : MySQL
 Source Server Version : 80022
 Source Host           : localhost:3306
 Source Schema         : testbank7

 Target Server Type    : MySQL
 Target Server Version : 80022
 File Encoding         : 65001

 Date: 27/03/2021 17:45:57
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_knowledge_copy1
-- ----------------------------
DROP TABLE IF EXISTS `t_knowledge_copy1`;
CREATE TABLE `t_knowledge_copy1`  (
  `knowledgeId` int NOT NULL AUTO_INCREMENT,
  `knowledgeUuid` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `parentKnowledgeId` int NULL DEFAULT NULL,
  `parentKnowledgeUuid` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `knowledgeLevel` int NULL DEFAULT 3,
  `knowledgeName` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `subject` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `grade` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `term` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `version` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `phase` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `area` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `areaUuid` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `topic` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `innerOrder` int NULL DEFAULT NULL,
  `questionNum` int NULL DEFAULT NULL,
  `status` int NULL DEFAULT NULL,
  `leafFlag` int NULL DEFAULT 1 COMMENT '0 目录节点 1 叶子节点',
  `remark` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`knowledgeId`) USING BTREE,
  INDEX `KnowledgeUuid`(`knowledgeUuid`) USING BTREE,
  INDEX `all`(`subject`, `grade`, `version`, `phase`, `area`, `topic`) USING BTREE,
  INDEX `knowledgeId`(`knowledgeId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 47763 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_knowledge_mathematics_probability
-- ----------------------------
DROP TABLE IF EXISTS `t_knowledge_mathematics_probability`;
CREATE TABLE `t_knowledge_mathematics_probability`  (
  `knowledgeId` int NOT NULL,
  `probability` double(11, 8) NOT NULL DEFAULT 0.00000000,
  PRIMARY KEY (`knowledgeId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_knowledge_mathematics_word_probability
-- ----------------------------
DROP TABLE IF EXISTS `t_knowledge_mathematics_word_probability`;
CREATE TABLE `t_knowledge_mathematics_word_probability`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `wordKid` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `probability` double(11, 8) NOT NULL DEFAULT 0.00000000,
  `modified_pro` double(11, 8) NOT NULL DEFAULT 0.00000000,
  `adaboost_pro` double(11, 8) NOT NULL DEFAULT 0.00000000,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `wordKid`(`wordKid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_knowledge_question_mathematics_copy1
-- ----------------------------
DROP TABLE IF EXISTS `t_knowledge_question_mathematics_copy1`;
CREATE TABLE `t_knowledge_question_mathematics_copy1`  (
  `knowledgeId` int NOT NULL DEFAULT 0,
  `questionId` int NOT NULL DEFAULT 0,
  `knowledgeUuid` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `questionUuid` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `subject` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int NULL DEFAULT NULL,
  PRIMARY KEY (`knowledgeId`, `questionId`) USING BTREE,
  INDEX `knowledgeUuid_2`(`knowledgeUuid`, `subject`, `status`) USING BTREE,
  INDEX `knowledgeUuid`(`knowledgeUuid`, `questionUuid`) USING BTREE,
  INDEX `idx_knowledge_questionid`(`questionUuid`) USING BTREE,
  INDEX `knowledgeId`(`knowledgeUuid`) USING BTREE,
  INDEX `questionId`(`questionId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_knowledge_question_mathematics_test
-- ----------------------------
DROP TABLE IF EXISTS `t_knowledge_question_mathematics_test`;
CREATE TABLE `t_knowledge_question_mathematics_test`  (
  `knowledgeId` int NOT NULL DEFAULT 0,
  `questionId` int NOT NULL DEFAULT 0,
  `knowledgeUuid` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `questionUuid` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `subject` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_question_content_mathematics_copy1
-- ----------------------------
DROP TABLE IF EXISTS `t_question_content_mathematics_copy1`;
CREATE TABLE `t_question_content_mathematics_copy1`  (
  `questionId` int NOT NULL DEFAULT 0,
  `questionUuid` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `analyse` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `htmlAnalyse` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `reply` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `htmlReply` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `appraise` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `htmlAppraise` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `answer` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `htmlAnswer` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `htmlContent` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `tips` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `htmlTips` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`questionId`) USING BTREE,
  UNIQUE INDEX `questionUuid`(`questionUuid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_question_content_mathematics_count
-- ----------------------------
DROP TABLE IF EXISTS `t_question_content_mathematics_count`;
CREATE TABLE `t_question_content_mathematics_count`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `word` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `count` int NULL DEFAULT 0,
  `tf` double(11, 2) NULL DEFAULT NULL,
  `allQuestions` int NULL DEFAULT NULL,
  `questionCount` int NULL DEFAULT NULL,
  `idf` double(11, 2) NULL DEFAULT NULL,
  `tfidf` double(11, 2) NULL DEFAULT NULL,
  `modifiedTfIdf` double(11, 2) NULL DEFAULT NULL,
  `entropy` double(11, 2) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_question_content_mathematics_seg
-- ----------------------------
DROP TABLE IF EXISTS `t_question_content_mathematics_seg`;
CREATE TABLE `t_question_content_mathematics_seg`  (
  `Id` int NOT NULL AUTO_INCREMENT,
  `questionId` int NULL DEFAULT NULL,
  `raw_content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `seg_content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `after_filter_content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `length` int NOT NULL DEFAULT 0,
  `item` char(2) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '0',
  `segType` int NULL DEFAULT 0 COMMENT '鍒嗙被宸ュ叿0-娓呭崕鍒嗚瘝锛?-缁撳反鍒嗚瘝锛?琛ㄧずhanlp鍒嗚瘝',
  `lenType` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`Id`) USING BTREE,
  UNIQUE INDEX `index_qid`(`questionId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19164 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_question_content_mathematics_seg_train
-- ----------------------------
DROP TABLE IF EXISTS `t_question_content_mathematics_seg_train`;
CREATE TABLE `t_question_content_mathematics_seg_train`  (
  `Id` int NOT NULL AUTO_INCREMENT,
  `questionId` int NULL DEFAULT NULL,
  `raw_content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `seg_content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `after_filter_content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `length` int NOT NULL DEFAULT 0,
  `item` char(2) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '0',
  `segType` int NULL DEFAULT 0 COMMENT '鍒嗙被宸ュ叿0-娓呭崕鍒嗚瘝锛?-缁撳反鍒嗚瘝锛?琛ㄧずhanlp鍒嗚瘝',
  `lenType` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`Id`) USING BTREE,
  UNIQUE INDEX `index_qid`(`questionId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15681 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_question_content_mathematics_test
-- ----------------------------
DROP TABLE IF EXISTS `t_question_content_mathematics_test`;
CREATE TABLE `t_question_content_mathematics_test`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `questionId` int NULL DEFAULT NULL,
  `content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `index_qid`(`questionId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1884 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_question_content_mathematics_train
-- ----------------------------
DROP TABLE IF EXISTS `t_question_content_mathematics_train`;
CREATE TABLE `t_question_content_mathematics_train`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `questionId` int NULL DEFAULT NULL,
  `content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `index_qid`(`questionId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16953 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_question_item_mathematics_copy1
-- ----------------------------
DROP TABLE IF EXISTS `t_question_item_mathematics_copy1`;
CREATE TABLE `t_question_item_mathematics_copy1`  (
  `itemId` int NOT NULL DEFAULT 0,
  `itemUuid` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `questionId` int NULL DEFAULT NULL,
  `questionUuid` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `innerOrder` int NULL DEFAULT NULL,
  `rightFlag` int NULL DEFAULT NULL,
  `score` float NULL DEFAULT NULL,
  `status` int NULL DEFAULT NULL,
  `htmlContent` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `content` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`itemId`) USING BTREE,
  INDEX `questionUuid`(`questionUuid`) USING BTREE,
  INDEX `questionId`(`questionId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_question_item_mathematics_seg
-- ----------------------------
DROP TABLE IF EXISTS `t_question_item_mathematics_seg`;
CREATE TABLE `t_question_item_mathematics_seg`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `questionId` int NULL DEFAULT NULL,
  `itemId` int NULL DEFAULT NULL,
  `raw_content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `seg_content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `after_filter_content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `length` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23242 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_question_item_mathematics_seg_train
-- ----------------------------
DROP TABLE IF EXISTS `t_question_item_mathematics_seg_train`;
CREATE TABLE `t_question_item_mathematics_seg_train`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `questionId` int NULL DEFAULT NULL,
  `itemId` int NULL DEFAULT NULL,
  `raw_content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `seg_content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `after_filter_content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `length` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19072 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_question_item_mathematics_test
-- ----------------------------
DROP TABLE IF EXISTS `t_question_item_mathematics_test`;
CREATE TABLE `t_question_item_mathematics_test`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `itemId` int NOT NULL,
  `questionId` int NOT NULL,
  `content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`, `itemId`) USING BTREE,
  UNIQUE INDEX `index_itemid`(`itemId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3265 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_question_item_mathematics_train
-- ----------------------------
DROP TABLE IF EXISTS `t_question_item_mathematics_train`;
CREATE TABLE `t_question_item_mathematics_train`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `itemId` int NOT NULL DEFAULT 0,
  `questionId` int NULL DEFAULT NULL,
  `innerOrder` int NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`id`, `itemId`) USING BTREE,
  UNIQUE INDEX `index_itemid`(`itemId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29405 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_question_mathematics
-- ----------------------------
DROP TABLE IF EXISTS `t_question_mathematics`;
CREATE TABLE `t_question_mathematics`  (
  `questionId` int NOT NULL AUTO_INCREMENT,
  `questionUuid` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `questionNo` int NULL DEFAULT NULL,
  `subject` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `phase` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `grade` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int NULL DEFAULT NULL,
  `typeValue` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `parentQuestionId` int NULL DEFAULT NULL,
  `parentQuestionUuid` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `innerOrder` int NULL DEFAULT NULL,
  `hasChild` int NULL DEFAULT NULL,
  `difficulty` int NULL DEFAULT NULL,
  `category` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `questionYear` int NULL DEFAULT NULL,
  `useTimes` int NULL DEFAULT NULL,
  `province` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `city` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `remark` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `createUserId` int NULL DEFAULT NULL,
  `createUserUuid` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `createTime` datetime NULL DEFAULT NULL,
  `auditUserId` int NULL DEFAULT NULL,
  `auditUserUuid` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `auditTime` datetime NULL DEFAULT NULL,
  `modifyUserId` int NULL DEFAULT NULL,
  `modifyTime` datetime NULL DEFAULT NULL,
  `source` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `accuracy` int NULL DEFAULT 0,
  `rightTimes` int NULL DEFAULT 0,
  `wrongTimes` int NULL DEFAULT 0,
  `undoTimes` int NULL DEFAULT 0,
  `paperTimes` int NULL DEFAULT 0,
  `useCount` int NULL DEFAULT 0,
  `itemRule` int NULL DEFAULT 3,
  `questionScore` int NULL DEFAULT NULL,
  PRIMARY KEY (`questionId`) USING BTREE,
  UNIQUE INDEX `questionUuid`(`questionUuid`) USING BTREE,
  INDEX `subject`(`subject`, `phase`, `grade`, `status`, `typeValue`) USING BTREE,
  INDEX `createTime_sort`(`createTime`) USING BTREE,
  INDEX `useTimes_sort`(`useTimes`) USING BTREE,
  INDEX `accuracy_sort`(`accuracy`) USING BTREE,
  INDEX `properties`(`status`, `typeValue`, `difficulty`, `category`, `source`, `city`, `questionYear`) USING BTREE,
  INDEX `qType_subject_grade`(`subject`, `grade`) USING BTREE,
  INDEX `status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6951917 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_question_mathematics_word
-- ----------------------------
DROP TABLE IF EXISTS `t_question_mathematics_word`;
CREATE TABLE `t_question_mathematics_word`  (
  `questionId` int NOT NULL,
  `words` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`questionId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_question_mathematics_word_test
-- ----------------------------
DROP TABLE IF EXISTS `t_question_mathematics_word_test`;
CREATE TABLE `t_question_mathematics_word_test`  (
  `questionId` int NOT NULL,
  `words` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`questionId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
