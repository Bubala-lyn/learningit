package segment;

import java.util.ArrayList;
import java.util.Vector;

import base.POCGraph;
import base.SegmentedSentence;
import base.TaggedSentence;
import base.WordWithTag;
import character.CBTaggingDecoder;
import manage.Filter;
import manage.NegWord;
import manage.Postprocesser;
import manage.Preprocesser;
import manage.Punctuation;
import manage.TimeWord;
import manage.VerbWord;
import system.SystemConf;
import utils.CommonUtils;

public class Thulac implements SegmentationService {
	private String prefix;
	private Character separator = '/';
	private CBTaggingDecoder cws_tagging_decoder;
	private SegmentedSentence segged;
	private CBTaggingDecoder tagging_decoder;
	private TimeWord timeword;
	private Preprocesser preprocesser;
	private POCGraph poc_cands;
	private TaggedSentence tagged;
	private Postprocesser nsDict;
	private Postprocesser idiomDict;
	private Punctuation punctuation;
	private NegWord negword;
	private VerbWord verbword;
	private boolean seg_only;
	private boolean useFilter;
	private Postprocesser userDict;
	private Filter filter;

	public Thulac(boolean segOnly, boolean useFilter) {
		this.prefix = SystemConf.getValueByCode("modelFilePrefix");
		this.seg_only = segOnly;
		this.useFilter = useFilter;
		try {
			preprocesser = new Preprocesser();
			poc_cands = new POCGraph();
			tagged = new TaggedSentence();
			nsDict = new Postprocesser((prefix + "ns.dat"), "ns", false);
			idiomDict = new Postprocesser((prefix + "idiom.dat"), "i", false);
			punctuation = new Punctuation((prefix + "singlepun.dat"));
			negword = new NegWord((prefix + "neg.dat"));
			verbword = new VerbWord((prefix + "vM.dat"), (prefix + "vD.dat"));
			preprocesser.setT2SMap((prefix + "t2s.dat"));

			if (seg_only) {
				cws_tagging_decoder = new CBTaggingDecoder();
				cws_tagging_decoder.threshold = 0;
				cws_tagging_decoder.separator = separator.charValue();
				cws_tagging_decoder.init(prefix + "cws_model.bin", prefix + "cws_dat.bin", prefix + "cws_label.txt");
				cws_tagging_decoder.setLabelTrans();
				segged = new SegmentedSentence();
				timeword = new TimeWord();
			} else {
				tagging_decoder = new CBTaggingDecoder();
				tagging_decoder.threshold = 10000;
				tagging_decoder.separator = separator.charValue();
				tagging_decoder.init(prefix + "model_c_model.bin", prefix + "model_c_dat.bin",
						prefix + "model_c_label.txt");
				tagging_decoder.setLabelTrans();
			}
			if (this.useFilter) {
				filter = new Filter(prefix + "xu.dat", prefix + "time.dat");
				userDict = new Postprocesser(SystemConf.getValueByCode("kwFile"), "uw", true);
			}
		} catch (Exception e) {
			System.out.println("Thulac:" + e.toString());
		}
	}

	public String[] segment(ArrayList<ArrayList<String>> words) throws Exception {
		String oiraw;
		String raw = new String();
		Vector<String> vec = null;
		ArrayList<String> list = new ArrayList<>(words.size());
		for (int k = 0; k < words.size(); k++) {
			vec = CommonUtils.getRaw(words.get(k), maxLength);
			if (vec.size() == 0) {
				list.add("");
				continue;
			}
			if (vec.size() > 1) {
				System.out.println(words.get(k).get(0));
			}
			// vec.size()=1
			StringBuilder sBuilder = new StringBuilder("");
			for (int i = 0; i < vec.size(); i++) {
				oiraw = vec.get(i);
				raw = preprocesser.clean(oiraw, poc_cands);
				if (raw.length() > 0) {
					if (seg_only) {
						cws_tagging_decoder.segment(raw, poc_cands, tagged);
						cws_tagging_decoder.get_seg_result(segged);
						nsDict.adjust(segged);
						idiomDict.adjust(segged);
						punctuation.adjust(segged);
						timeword.adjust(segged);
						negword.adjust(segged);
						if (useFilter) {
							userDict.adjust(segged);
							filter.adjust(segged);
						}
						for (int j = 0; j < segged.size(); j++)
							sBuilder.append(segged.get(j) + " ");
						list.add(sBuilder.toString());
					} else {
						tagging_decoder.segment(raw, poc_cands, tagged);
						nsDict.adjust(tagged);
						idiomDict.adjust(tagged);
						punctuation.adjust(tagged);
						negword.adjust(tagged);
						verbword.adjust(tagged);
						if (useFilter) {
							userDict.adjust(tagged);
							filter.adjust(tagged);
						}
						for (int j = 0; j < tagged.size(); j++) {
							WordWithTag wwt = tagged.get(j);
							if (!(wwt.tag.equals("w") || wwt.tag.equals("o") || wwt.tag.equals("e")
									|| wwt.tag.equals("c") || wwt.tag.equals("u") || wwt.tag.equals("y"))
									&& (wwt.tag.equals("n") || wwt.tag.equals("uw") || wwt.tag.equals("v")
											|| wwt.tag.equals("id")))
								sBuilder.append(wwt.word + " ");
						}
						list.add(sBuilder.toString());
					}
				}
			}
		}
		int size = list.size();
		return (String[]) list.toArray(new String[size]);
	}

	@Override
	public String segment(String text) {
		String oiraw;
		String raw = new String();
		Vector<String> vec = null;
		StringBuilder sBuilder = new StringBuilder("");
		ArrayList<String> words = new ArrayList<>(2);
		words.add(null);
		words.add(text);
		vec = CommonUtils.getRaw(words, maxLength);
		if (vec.size() == 0)
			return null;
		for (int i = 0; i < vec.size(); i++) {
			oiraw = vec.get(i);
			raw = preprocesser.clean(oiraw, poc_cands);
			if (raw.length() > 0) {
				if (seg_only) {
					cws_tagging_decoder.segment(raw, poc_cands, tagged);
					cws_tagging_decoder.get_seg_result(segged);
					nsDict.adjust(segged);
					idiomDict.adjust(segged);
					punctuation.adjust(segged);
					timeword.adjust(segged);
					negword.adjust(segged);
					if (useFilter) {
						userDict.adjust(segged);
						filter.adjust(segged);
					}
					for (int j = 0; j < segged.size(); j++)
						sBuilder.append(segged.get(j));
				} else {
					tagging_decoder.segment(raw, poc_cands, tagged);
					nsDict.adjust(tagged);
					idiomDict.adjust(tagged);
					punctuation.adjust(tagged);
					negword.adjust(tagged);
					verbword.adjust(tagged);
					if (useFilter) {
						userDict.adjust(tagged);
						filter.adjust(tagged);
					}
					for (int j = 0; j < tagged.size(); j++) {
						WordWithTag wwt = tagged.get(j);
						if (!(wwt.tag.equals("w") || wwt.tag.equals("o") || wwt.tag.equals("e") || wwt.tag.equals("c")
								|| wwt.tag.equals("u") || wwt.tag.equals("y"))
								&& (wwt.tag.equals("n") || wwt.tag.equals("uw") || wwt.tag.equals("v")
										|| wwt.tag.equals("id")))
							sBuilder.append(wwt.word + " ");
					}
				}
			}
		}
		return CommonUtils.filter(" ", sBuilder.toString());
	}
}
