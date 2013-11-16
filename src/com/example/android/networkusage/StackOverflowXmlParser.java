/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.android.networkusage;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class parses XML feeds from stackoverflow.com.
 * Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 */
public class StackOverflowXmlParser {
    private static final String ns = null;
    // We don't use namespaces

    public List<Sale> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            // TODO replace readFeed with readSalesFeed
            return readSalesFeed(parser);
        } finally {
            in.close();
        }
    }
    
    public List<Sale> readSalesFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
    	List<Sale> sales = new ArrayList<Sale>();
    	
    	parser.require(XmlPullParser.START_TAG, ns, "latest");
    	while (parser.next() != XmlPullParser.END_TAG){
    		if (parser.getEventType() != XmlPullParser.START_TAG){
    			continue;
    		}
    		
    		// TODO substring sale tag - let see if this works
    		String name = parser.getName();
//    		name = name.substring(0, 5);
    		
    		// Starts by looking for the entry tag
    		if (name.equals("sale")){
    			sales.add(readSale(parser));
    		} else {
    			skip(parser);
    		}
    	}
    	
    	return sales;
    }

    private Sale readSale(XmlPullParser parser) throws XmlPullParserException, IOException {
    	// TODO figure out how to substring "sale"
  		// String name = parser.getName();
		// name = name.substring(0, 5);
    	parser.require(XmlPullParser.START_TAG, ns, "sale");
    	
    	String earnings = null;
    	String title = null;
    	String thumbnailUrl = null;
    	
    	while (parser.next() != XmlPullParser.END_TAG){
    		if (parser.getEventType() != XmlPullParser.START_TAG){
    			continue;
    		}
    		String name = parser.getName();
    		if (name.equals("earnings")){
    			earnings = readEarnings(parser);
    		} else if (name.equals("title")){
    			// readTitle can be reused
    			earnings = readTitle(parser);
    		} else if (name.equals("thumbnailUrl")){
    			thumbnailUrl = readThumbnailUrl(parser);
    		} else {
    			// the Entry skip can be reused
    			skip(parser);
    		}
    	}
    	
		return new Sale(earnings, title, thumbnailUrl);
	}

	private String readThumbnailUrl(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "thumbnailUrl");
		String url = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "thumbnailUrl");
		return url;
	}

	private String readEarnings(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "earnings");
        String earnings = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "earnings");
        return earnings;
	}

	private List<Entry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Entry> entries = new ArrayList<Entry>();

        parser.require(XmlPullParser.START_TAG, ns, "feed");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("entry")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    // This class represents a single entry (post) in the XML feed.
    // It includes the data members "title," "link," and "summary."
    public static class Entry {
        public final String title;
        public final String link;
        public final String summary;

        private Entry(String title, String summary, String link) {
            this.title = title;
            this.summary = summary;
            this.link = link;
        }
    }
    
    public static class Sale {
    	public final String earnings;
    	public final String title;
    	public final String thumbnailUrl;
    	
    	private Sale(String earnings, String title,String thumbnailUrl){
    		this.earnings = earnings;
    		this.title = title;
    		this.thumbnailUrl = thumbnailUrl;
    	}
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them
    // off
    // to their respective &quot;read&quot; methods for processing. Otherwise, skips the tag.
    private Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "entry");
        String title = null;
        String summary = null;
        String link = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
                title = readTitle(parser);
            } else if (name.equals("summary")) {
                summary = readSummary(parser);
            } else if (name.equals("link")) {
                link = readLink(parser);
            } else {
                skip(parser);
            }
        }
        return new Entry(title, summary, link);
    }

    // Processes title tags in the feed.
    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");
        return title;
    }

    // Processes link tags in the feed.
    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        String link = "";
        parser.require(XmlPullParser.START_TAG, ns, "link");
        String tag = parser.getName();
        String relType = parser.getAttributeValue(null, "rel");
        if (tag.equals("link")) {
            if (relType.equals("alternate")) {
                link = parser.getAttributeValue(null, "href");
                parser.nextTag();
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "link");
        return link;
    }

    // Processes summary tags in the feed.
    private String readSummary(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "summary");
        String summary = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "summary");
        return summary;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
            case XmlPullParser.END_TAG:
                    depth--;
                    break;
            case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
