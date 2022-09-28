package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dao.DbChannel;
import po.KWeight;
import po.KnowledgeNode;
import po.Result;
import system.Constant;
import system.SystemConf;
import utils.CommonUtils;

public class SqlDao implements DbChannel {
	String url = SystemConf.getValueByCode("url");;
	private Connection conn = null;

	public SqlDao() {
		this.init();
	}
	public SqlDao(String url) {
		this.url = url;
		this.init();
	}

	private void init() {
		try {
			Class.forName(SystemConf.getValueByCode("driver"));
			conn = DriverManager.getConnection(this.url, SystemConf.getValueByCode("user"),
					SystemConf.getValueByCode("password"));
			// conn.setAutoCommit(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getAllRecords(String table) {
		ResultSet result = null;
		int sum = 0;
		try {
			String sql = "select count(1) as sum from " + table;
			Statement state = conn.createStatement();
			result = state.executeQuery(sql);
			result.next();
			sum = result.getInt("sum");
			result.close();
		} catch (Exception e) {
		}
		return sum;
	}

	public ArrayList<ArrayList<String>> getFilterContent(String sql, String[] args, int runWhich) {
		ResultSet resultSet = null;
		ArrayList<ArrayList<String>> list = null;
		try {
			Statement state = conn.createStatement();
			resultSet = state.executeQuery(sql);
			resultSet.last();
			int lines = resultSet.getRow();
			resultSet.beforeFirst();
			list = new ArrayList<>(lines);
			while (resultSet.next()) {
				String temp = new String();
				switch (runWhich) {
				case 0:
					temp = CommonUtils.translate(resultSet.getString(args[0]), resultSet.getString(args[1]));
					break;
				case 1:
					temp = resultSet.getString(args[1]);
					break;
				default:
					break;
				}
				if (temp.length() < 1)// 此处有诈
					continue;
				String[] tmpArr = temp.split(" ");
				ArrayList<String> items = new ArrayList<>(tmpArr.length);
				String qid = resultSet.getString(args[0]);
				if (qid == null)
					System.out.println("qid为空，出错了....");
				items.add(qid);
				for (String item : tmpArr)
					items.add(item);
				list.add(items);
			}
			resultSet.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public void insert(String sql, String[][] datas) {
		int j = 0;
		try {
			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement(sql);
			for (String[] data : datas) {
				try {
					int i = 1;
					for (String item : data)
						pstmt.setString(i++, item);
					pstmt.addBatch();
					if (j++ == 1000) {
						j = 0;
						pstmt.executeBatch();
					}
				} catch (Exception e) {
					System.out.println("SqlDao:insert:" + e.toString() + " " + data[0] + " " + data[1]);
				}
			}
			pstmt.executeBatch();
			conn.commit();
			pstmt.clearBatch();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insert(String sql, HashMap<Integer, String> datas) {
		int j = 0;
		try {
			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement(sql);
			for (Map.Entry<Integer, String> entry : datas.entrySet()) {
				try {
					pstmt.setInt(1, entry.getKey());
					pstmt.setString(2, entry.getValue());
					pstmt.addBatch();
					if (j++ == 1000) {
						j = 0;
						pstmt.executeBatch();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			pstmt.executeBatch();
			conn.commit();
			pstmt.clearBatch();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insertMap(String sql, HashMap<Integer, List<Result>> map) {
		int t = 0;
		PreparedStatement pstmt = null;
		try {
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(sql);
			Iterator<Entry<Integer, List<Result>>> iter = map.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Integer, List<Result>> entry = iter.next();
				Integer qid = entry.getKey();
				List<Result> res = entry.getValue();
				for (Result rs : res) {
					pstmt.setInt(1, qid);
					pstmt.setInt(2, Integer.parseInt(rs.getKnowledgeId()));
					pstmt.setString(3, rs.getKnowledgeName());
					pstmt.setDouble(4, rs.getPercentage());
					pstmt.addBatch();
					if (++t == 1000) {
						pstmt.executeBatch();
						pstmt.clearBatch();
						t = 0;
					}
				}
				pstmt.executeBatch();
				conn.commit();
				pstmt.clearBatch();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insertPresionAndRecallAndFvalue(String sql, HashMap<Integer, List<Double>> map) {
		int t = 0;
		PreparedStatement pstmt = null;
		try {
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(sql);
			Iterator<Entry<Integer, List<Double>>> iter = map.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Integer, List<Double>> entry = iter.next();
				Integer qid = entry.getKey();
				List<Double> res = entry.getValue();
				pstmt.setInt(1, qid);
				pstmt.setDouble(2, res.get(0));
				pstmt.setDouble(3, res.get(1));
				pstmt.setDouble(4, res.get(2));
				pstmt.addBatch();
				if (++t == 1000) {
					pstmt.executeBatch();
					pstmt.clearBatch();
					t = 0;
				}
			}
			pstmt.executeBatch();
			conn.commit();
			pstmt.clearBatch();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void delete(String table, boolean autoIncrementFlush) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.execute("delete from " + table);
			if (autoIncrementFlush)
				stmt.execute("ALTER TABLE " + table + " AUTO_INCREMENT = 1");
			// conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("SQLExecute Error...");
		}
	}

	public void truncate(String table) {
		String sql = "truncate table " + table;
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.execute();
			// conn.commit();
			System.out.println("truncate done");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("SQLExecute Truncate Error...");
		}
	}

	public void update(String sql) {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.execute();
			// conn.commit();
			System.out.println("update done");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("SQLExecute Update Error...");
		}
	}

	public void batchUpdate(List<String> sqlList) {
		if (sqlList.isEmpty())
			return;
		Statement stmt = null;
		try {
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			for (int i = 0; i < sqlList.size(); i++) {
				if (i != 0 && i % 1000 == 0)
					stmt.executeBatch();
				stmt.addBatch(sqlList.get(i));
			}
			stmt.executeBatch();
			conn.commit();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<String> getAllValidKnowledges() {
		List<String> list = new ArrayList<>();
		ResultSet result = null;
		Statement state = null;
		String sql = null;
		try {
			state = conn.createStatement();
			result = state.executeQuery(SystemConf.getValueByCode("selectValidKnowledgeIdSql"));
			if (result.next())
				sql = result.getString(1);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		try {
			state = conn.createStatement();
			result = state.executeQuery(sql);
			while (result.next())
				list.add(result.getString(1));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}

	public List<String> getAllValidTrainKnowledges() {
		List<String> list = new ArrayList<>();
		ResultSet result = null;
		try {
			Statement state = conn.createStatement();
			result = state.executeQuery(SystemConf.getValueByCode("selectValidTrainKnowledgeIdSql"));
			while (result.next())
				list.add(result.getString(1));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}

	/**
	 * 返回结果集为map结构的数据
	 */
	public Map<String, Double> getMapResult(String sql) {
		Map<String, Double> map = new HashMap<String, Double>();
		ResultSet result = null;
		try {
			Statement state = conn.createStatement();
			result = state.executeQuery(sql);
			while (result.next())
				map.put(result.getString(1), result.getDouble(2));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return map;
	}

	public int insertKnowledgeProbability(String sql, int count) {
		int rtn = 0;
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, count);
			rtn = pstmt.executeUpdate();
			// conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return rtn;
	}

	/**
	 * 
	 * @param sql
	 * @param keyIndex
	 *            主键下标
	 * @param valueIndex
	 *            更新值下标
	 * @param map
	 */
	public void insertOrUpdateCommand(String sql, int keyIndex, int valueIndex, Map<String, Double> map) {
		if (map == null)
			return;
		String key = null;
		Double value = null;
		PreparedStatement pstmt = null;
		try {
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(sql);
			int t = 0;
			for (Map.Entry<String, Double> entry : map.entrySet()) {
				key = entry.getKey();
				value = entry.getValue();
				if (value == 0.0)
					continue;
				if (key.length() > 30) {
					// System.out.println(key);
					continue;
				}
				pstmt.setString(keyIndex, key);
				pstmt.setDouble(valueIndex, value);
				pstmt.addBatch();
				if (++t == 1000) {
					pstmt.executeBatch();
					pstmt.clearBatch();
					t = 0;
				}
			}
			pstmt.executeBatch();
			conn.commit();
			pstmt.clearBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<ArrayList<String>> getContentsAndQuestionIdsList(String sql) {
		ResultSet resultSet = null;
		ArrayList<String> questionId = null;
		ArrayList<String> content = null;
		ArrayList<ArrayList<String>> all = new ArrayList<ArrayList<String>>(2);

		try {
			Statement state = conn.createStatement();
			resultSet = state.executeQuery(sql);
			resultSet.last();
			int lines = resultSet.getRow();
			resultSet.beforeFirst();
			questionId = new ArrayList<>(lines);
			content = new ArrayList<>(lines);
			while (resultSet.next()) {
				questionId.add(resultSet.getString("questionId"));
				content.add(resultSet.getString("content"));
			}
			all.add(questionId);
			all.add(content);
			resultSet.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return all;
	}

	public Map<String, ArrayList<String>> getAllFenciFromWord() {
		Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		String sql = "select knowledgeId,qcw.questionId,qcw.words from " + Constant.T_KNOWLEDGEWEIGHT + " left join "
				+ Constant.T_TRAIN_WORD
				+ " qcw using(questionId) where qcw.questionId is not null order by knowledgeId;";
		// String sql = "select knowledgeId,qcw.questionId,qcw.words from " +
		// Constant.T_KNOWLEDGEWEIGHT + " left join "
		// + Constant.T_TRAIN_WORD + " qcw using(questionId) where knowledgeId
		// in("
		// + SystemConf.getValueByCode("selectValidKnowledgeIdSql") + ") "
		// + " and qcw.questionId is not null order by knowledgeId;";
		System.out.println("getAllFenciFromWord->" + sql);
		ArrayList<String> list = null;
		ResultSet result = null;
		try {
			Statement state = conn.createStatement();
			result = state.executeQuery(sql);
			while (result.next()) {
				String key = result.getString("knowledgeId");
				if (map.containsKey(key)) {
					list = map.get(key);
					list.add(result.getString("words"));
					map.put(key, list);
				} else {
					list = new ArrayList<>();
					list.add(result.getString("words"));
					map.put(key, list);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return map;
	}

	public Map<String, Integer> getAllValidKnowledgeQuestionCount() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		String sql = "select knowledgeId,count(DISTINCT questionId) as count from t_knowledge_question_mathematics_copy1 WHERE questionId in (SELECT questionId FROM t_question_content_mathematics_train) group by knowledgeId ORDER BY knowledgeId;";
		ResultSet result = null;
		try {
			Statement state = conn.createStatement();
			result = state.executeQuery(sql);
			while (result.next())
				map.put(result.getString(1), result.getInt(2));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return map;
	}

	public Map<Integer, List<Integer>> getAllValidKnowledgeQuestionFromTrain() {
		Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
		String sql = "select knowledgeId,questionId from t_knowledge_question_mathematics_copy1 where questionId in(select questionId from t_question_content_mathematics_copy1) and questionId in(select questionId from t_question_mathematics_word) ORDER BY knowledgeId";
		ResultSet result = null;
		try {
			Statement state = conn.createStatement();
			result = state.executeQuery(sql);
			while (result.next()) {
				Integer key = result.getInt(1);
				Integer value = result.getInt(2);
				if (map.containsKey(key)) {
					List<Integer> list = map.get(key);
					list.add(value);
					map.put(key, list);
				} else {
					List<Integer> list = new ArrayList<Integer>();
					list.add(value);
					map.put(key, list);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return map;
	}

	public Map<String, String> getAllValidKnowledgeQuestionFromTrain2() {
		Map<String, String> map = new HashMap<String, String>();
		String sql = "select questionId,knowledgeId from t_knowledge_question_mathematics_copy1 where questionId in(select questionId from t_paper_question_content_mysubject_train) and questionId in(select questionId from t_question_mathematics_word) ORDER BY rand()";
		ResultSet result = null;
		try {
			Statement state = conn.createStatement();
			result = state.executeQuery(sql);
			while (result.next()) {
				String key = result.getString(1);
				String value = result.getString(2);
				map.put(key, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return map;
	}

	public Map<Integer, List<Integer>> getAllValidKnowledgeQuestionFromSource(String sql) {
		Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
		ResultSet result = null;
		try {
			Statement state = conn.createStatement();
			result = state.executeQuery(sql);
			while (result.next()) {
				Integer key = result.getInt(1);
				Integer value = result.getInt(2);
				if (map.containsKey(key)) {
					List<Integer> list = map.get(key);
					list.add(value);
					map.put(key, list);
				} else {
					List<Integer> list = new ArrayList<Integer>();
					list.add(value);
					map.put(key, list);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return map;
	}

	public List<String> getAllWordsFromCount() {
		String sql = "select word from " + Constant.T_TRAIN_CONTENTCOUNT;
		List<String> list = new ArrayList<>();
		ResultSet result = null;
		try {
			Statement state = conn.createStatement();
			result = state.executeQuery(sql);
			while (result.next())
				list.add(result.getString(1));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}

	public List<String> getKnowledgeNameByQuestionId(String qid) {
		String sql = "select knowledgeName from t_knowledge_copy1 where knowledgeId in(select knowledgeId "
				+ "from t_knowledge_question_mathematics_copy1 where questionId=?)";
		List<String> kName = null;
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			kName = new ArrayList<>();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, qid);
			result = pstmt.executeQuery();
			while (result.next())
				kName.add(result.getString(1));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return kName;
	}

	public Map<String, String> getKnowledgeNameByKnowledgeIds(List<Result> results) {
		StringBuffer sb = new StringBuffer(
				"select knowledgeId,knowledgeName from t_knowledge_copy1 where knowledgeId in(");
		if (results.size() == 0) {
			return null;
		}
		for (Result r : results)
			sb.append(r.getKnowledgeId()).append(",");
		sb.append("-1").append(")");
		Map<String, String> kName = null;
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			kName = new HashMap<String, String>();
			pstmt = conn.prepareStatement(sb.toString());
			result = pstmt.executeQuery();
			while (result.next())
				kName.put(result.getString(1), result.getString(2));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return kName;
	}

	public Map<String, String> getAllKnowledgeNames() {
		Map<String, String> map = new HashMap<String, String>();
		ResultSet result = null;
		try {
			Statement state = conn.createStatement();
			result = state.executeQuery(SystemConf.getValueByCode("selectValidTrainKnowledgeSql"));
			while (result.next())
				map.put(result.getString(1), result.getString(2));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return map;
	}

	public double[] getTfidfWeight(String[] strs) {
		double w[] = new double[strs.length];
		double sum = 0.0;
		String sql = "select word,tfidf from " + Constant.T_TRAIN_CONTENTCOUNT
				+ " where count>1 and questionCount>1 and word in(";
		for (String s : strs)
			sql += "'" + s + "',";
		sql += "-1);";
		HashMap<String, Double> map = null;
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			map = new HashMap<String, Double>();
			pstmt = conn.prepareStatement(sql);
			result = pstmt.executeQuery();
			while (result.next()) {
				map.put(result.getString(1), result.getDouble(2));
				sum += result.getDouble(2);
			}
			for (int i = 0; i < strs.length; i++)
				w[i] = map.containsKey(strs[i]) ? ((double) map.get(strs[i]) / sum) : 0;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return w;
	}

	public Map<String, Object> getKeyWordsByQIdAndTfidf(String str, int k, int qid) {
		if (k == 0)
			throw new IllegalArgumentException("Argument Error!");
		String sql = "select words from " + Constant.T_TRAIN_WORD + " where questionId=?";
		String sql2 = "select word,tfidf from " + Constant.T_TRAIN_CONTENTCOUNT + " where word in(";
		Map<String, Object> rtnMap = new HashMap<String, Object>();
		Map<String, Double> internMap = new HashMap<String, Double>();
		PreparedStatement pstmt = null;
		ResultSet result = null;
		int size = 0, i = 0;
		try {
			if (null == str) {
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, qid);
				result = pstmt.executeQuery();
				if (result.next())
					str = result.getString(1);
				str = str.replace(",", " ");
			}
			String tmp[] = str.split(" ");
			size = k < 0 ? tmp.length : k > tmp.length ? tmp.length : k;
			for (String s : tmp)
				sql2 += "'" + s + "',";
			sql2 += "'-1') order by tfidf desc";
			str = "";
			pstmt = conn.prepareStatement(sql2);
			result = pstmt.executeQuery();
			while (result.next() && i++ < size) {
				str += result.getString(1) + " ";
				internMap.put(result.getString(1), result.getDouble(2));
			}
			rtnMap.put("text", str.trim());
			rtnMap.put("tfidfKV", internMap);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return rtnMap;
	}

	public Map<Integer, String> getAllQuestions(String sql) {
		Map<Integer, String> ques = null;
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			ques = new HashMap<Integer, String>();
			pstmt = conn.prepareStatement(sql);
			result = pstmt.executeQuery();
			while (result.next()) {
				String value = result.getString(2);
				value = value.trim();
				if (!"".equals(value))
					ques.put(result.getInt(1), result.getString(2));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ques;
	}

	public Map<Integer, String> getOneQuestion(String sql) {
		Map<Integer, String> ques = null;
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			ques = new HashMap<Integer, String>();
			pstmt = conn.prepareStatement(sql);
			result = pstmt.executeQuery();
			while (result.next())
				ques.put(result.getInt(1), result.getString(2));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ques;
	}

	public Map<Integer, List<String>> getAllQuestionWeightFromTrain(String sql) {
		Map<Integer, List<String>> ques = null;
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			ques = new HashMap<Integer, List<String>>();
			pstmt = conn.prepareStatement(sql);
			result = pstmt.executeQuery();
			while (result.next()) {
				List<String> list = new ArrayList<String>();
				if (ques.containsKey(result.getInt(1))) {
					list = ques.get(result.getInt(1));
				}
				list.add(result.getString(2));
				// 注意这里把每个题目所属知识点的权重注释掉了
				// 在BatchTest.java类那里有可能用到，可以放开
				// list.add(result.getString(3));
				ques.put(result.getInt(1), list);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ques;
	}

	public Map<Integer, List<KWeight>> getAllQuestionWeight() {
		Map<Integer, List<KWeight>> ques = null;
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			ques = new HashMap<Integer, List<KWeight>>();
			pstmt = conn.prepareStatement(SystemConf.getValueByCode("selectQuestionWeightFromTrainSql"));
			result = pstmt.executeQuery();
			while (result.next()) {
				KWeight kw = new KWeight();
				kw.realKey = result.getString(2);
				kw.realProbability = result.getInt(3);
				List<KWeight> list = new ArrayList<KWeight>();
				if (ques.containsKey(result.getInt(1))) {
					list = ques.get(result.getInt(1));
				}
				list.add(kw);
				ques.put(result.getInt(1), list);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ques;
	}

	public int[] getAllTestQuestionIds() {
		int[] qids = new int[101];
		String sql = "select questionId from t_question_content_mysubject order by rand();";
		PreparedStatement pstmt = null;
		ResultSet result = null;
		int i = 1;
		try {
			pstmt = conn.prepareStatement(sql);
			result = pstmt.executeQuery();
			while (result.next() && i < 101)
				qids[i++] = result.getInt(1);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return qids;
	}

	public List<Integer> getAllQuestionIds(String table) {
		String sql = "select distinct questionId from " + table;
		List<Integer> qids = new ArrayList<Integer>();
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = conn.prepareStatement(sql);
			result = pstmt.executeQuery();
			while (result.next())
				qids.add(result.getInt(1));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return qids;
	}

	public List<Integer> getAllTestQuestionIdsFromMysubject(String sql) {
		List<Integer> qids = new ArrayList<Integer>();
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = conn.prepareStatement(sql);
			result = pstmt.executeQuery();
			while (result.next())
				qids.add(result.getInt(1));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return qids;
	}

	/**
	 * 
	 * @param type：0
	 *            for test others for t_question_content_mysubject
	 * @param qid
	 * @return
	 */
	public String getTextByQid(boolean isTest, int qid) {
		String sql;
		String itemSql;
		if (isTest) {
			sql = "select questionId,content from t_question_content_mathematics_test where questionId=" + qid;
			itemSql = "select questionId,content from t_question_item_mathematics_test where questionId=" + qid;
		} else {
			sql = "select questionId,content from t_paper_question_content_mysubject_train where questionId=" + qid;
			itemSql = "select questionId,content from t_paper_question_item_mysubject_train where questionId=" + qid;
		}
		ArrayList<ArrayList<String>> all = getContentsAndQuestionIdsList(sql);
		ArrayList<ArrayList<String>> allItems = getContentsAndQuestionIdsList(itemSql);
		ArrayList<String> qids = all.get(0);
		ArrayList<String> itemQids = allItems.get(0);
		ArrayList<String> content = all.get(1);
		ArrayList<String> itemContent = allItems.get(1);
		String tmp = "";
		for (int i = 0; i < qids.size(); i++) {
			tmp = CommonUtils.translate(qids.get(i), content.get(i));
			tmp = CommonUtils.assemble(qids.get(i), tmp, itemQids, itemContent);
		}
		return tmp;
	}

	public HashMap<Integer, List<String>> getKnowledges(String table) {
		String sql = "select questionId,knowledgeId from " + table + " order by questionId;";
		HashMap<Integer, List<String>> map = new HashMap<Integer, List<String>>();
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = conn.prepareStatement(sql);
			result = pstmt.executeQuery();
			while (result.next()) {
				Integer key = result.getInt(1);
				if (map.containsKey(key)) {
					List<String> list = map.get(key);
					list.add(result.getString(2));
					map.put(key, list);
				} else {
					List<String> list = new ArrayList<String>();
					list.add(result.getString(2));
					map.put(key, list);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return map;
	}
	
	/**
	 * 
	 * @param cond 条件
	 * @return
	 */
	public List<String> getQuestionIdsWithCond(String cond) {
		String sql = "Select questionId From t_question_content_mathematics_copy1 " + cond;
		List<String> qids = new ArrayList<String>();
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = conn.prepareStatement(sql);
			result = pstmt.executeQuery();
			while (result.next())
				qids.add(result.getString(1));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return qids;
	}

	public double getMaxTFIDFByWords(String content) {
		String[] strs = content.split(" ");
		String seg = "";
		for (int i = 0; i < strs.length; i++)
			seg += "'" + strs[i] + "',";
		seg = seg.substring(0, seg.length() - 1);
		String sql = "select max(tfidf) from " + Constant.T_TRAIN_CONTENTCOUNT + " where word in(" + seg + ")";
		System.out.println("getMaxTFIDFByWords Sql->" + sql);
		double res = 0;
		Statement stmt = null;
		ResultSet result = null;
		try {
			stmt = conn.createStatement();
			result = stmt.executeQuery(sql);
			if (result.next())
				res = result.getDouble(1);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return res;
	}

	public String filterWords(double maxTFIDF, String content, int belta) {
		String[] strs = content.split(" ");
		String seg = "";
		for (int i = 0; i < strs.length; i++)
			seg += "'" + strs[i] + "',";
		seg = seg.substring(0, seg.length() - 1);
		String sql = "select word," + maxTFIDF + "/tfidf as b from " + Constant.T_TRAIN_CONTENTCOUNT + " where word in("
				+ seg + ")";
		System.out.println("filterWords Sql->" + sql);
		String res = "";
		Statement stmt = null;
		ResultSet result = null;
		try {
			stmt = conn.createStatement();
			result = stmt.executeQuery(sql);
			while (result.next()) {
				if (result.getDouble(2) < belta)
					res += result.getString(1) + ",";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return res.substring(0, res.length() - 1);
	}

	/**
	 * 获取词向量 讲tfidf值很小的去掉
	 * 
	 * @return
	 */
	public List<String> getPostList() {
		double belta = getMaxTFIDF() / 150;
		String sql = "select word from t_question_content_mathematics_count where tfidf>" + belta
				+ " order by tfidf desc;";
		List<String> list = null;
		Statement stmt = null;
		ResultSet result = null;
		try {
			list = new ArrayList<String>();
			stmt = conn.createStatement();
			result = stmt.executeQuery(sql);
			while (result.next()) {
				list.add(result.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}

	/**
	 * 将所有题目全部向量化 并且归一化处理
	 * 
	 * @return
	 */
	public Map<Integer, double[]> initAllQuestionsWordVec() {
		Map<Integer, double[]> map = null;
		String sql = "select questionId,words from t_question_mathematics_word";
		List<String> ws = getPostList();
		Statement stmt = null;
		ResultSet result = null;
		try {
			map = new HashMap<Integer, double[]>();
			stmt = conn.createStatement();
			result = stmt.executeQuery(sql);
			while (result.next()) {
				String[] strs = result.getString(2).split(",");
				double[] wc = new double[ws.size()];
				double sum = 0;
				for (String s : strs) {
					int index = ws.indexOf(s);
					if (index != -1) {
						wc[index]++;
						sum++;
					}
				}
				for (int i = 0; i < wc.length; i++)
					wc[i] /= sum;
				map.put(result.getInt(1), wc);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return map;
	}

	public List<String> getTfidfTopK(int topK) {
		String sql = "select word from t_question_content_mathematics_count order by tfidf desc limit 0,"
				+ topK;
		List<String> words = null;
		Statement stmt = null;
		ResultSet result = null;
		try {
			words = new ArrayList<>();
			stmt = conn.createStatement();
			result = stmt.executeQuery(sql);
			while (result.next()) {
				words.add(result.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return words;
	}

	private double getMaxTFIDF() {
		double res = 0;
		String sql = "select max(tfidf) from t_question_content_mathematics_count";
		Statement stmt = null;
		ResultSet result = null;
		try {
			stmt = conn.createStatement();
			result = stmt.executeQuery(sql);
			if (result.next()) {
				res = result.getDouble(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return res;
	}

	public int stamp() {
		int avg = 0;
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = conn.prepareStatement(SystemConf.getValueByCode("selectAvgLengthSegmentData"));
			result = pstmt.executeQuery();
			if (result.next()) {
				avg = result.getInt(1);
			}
			pstmt = conn.prepareStatement(SystemConf.getValueByCode("updateAvgLengthSegmentData"));
			pstmt.setInt(1, avg);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	public void close() {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Map<String, KnowledgeNode> getDistanceMap() {
		ResultSet result = null;
		Map<String, KnowledgeNode> nodes = new HashMap<String, KnowledgeNode>();
		nodes.put("0", null);
		try {
			Statement state = conn.createStatement();
			result = state.executeQuery(SystemConf.getValueByCode("selectDistanceKnowledgeSql"));
			while (result.next()) {
				// System.out.println(result.getString(1) + ", " + result.getString(2));
				nodes.put(result.getString(1), new KnowledgeNode(result.getString(1), result.getString(2)));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return nodes;
	}
}
