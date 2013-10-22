/**
 * Distributed Voronoi Diagram
 *
 *  @author Frederico Martins Biber Sampaio
 *
 * The MIT License (MIT)
 * 
 * Copyright (C) 2013  Frederico Martins Biber Sampaio
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE. 
*/
package algorithm;
import java.util.ArrayList;
import java.util.HashMap;

class RemoteSiteHistory {
    Msg lastMsg = null;
    int forwardHops = Integer.MAX_VALUE;
    int forwardChannel = -1;
}


public class LocalHistory {
    
    private HashMap<Site, RemoteSiteHistory> localHistory;
    private Site theSite;
    
    public LocalHistory(Site site) {
        this.theSite = site;
        localHistory = new HashMap<Site, RemoteSiteHistory>();
    }
    
    public Msg lastMsg(Site site) {
        RemoteSiteHistory history = localHistory.get(site);
        if (history != null)
            return history.lastMsg;
        return null;
    }
    
    public void clean(ArrayList<Site> sites) {
        for (Site site: sites)
            localHistory.remove(site);
    }
    
    public int forwardChannel(Site to) {
        RemoteSiteHistory history = localHistory.get(to);
        if (history != null)
            return history.forwardChannel;
        return -1;
    }
    
    public boolean register(Msg msg, int channelIn) {
        return (getLastMessage(msg, channelIn) != null);
    }

    // Get local history (control) of the remote site
    public Msg getLastMessage(Msg msg, int channelIn) {
        Site from = msg.getFrom();
        // Is remote?
        if (theSite.equals(from))
            // I do know everything i need about myself!
            return null;
        // Is site known?
        if (localHistory.containsKey(from)) {
            RemoteSiteHistory history = localHistory.get(from);
            Msg oldMsg = history.lastMsg;
            Site oldSite = oldMsg.getFrom();
            // Keep the smallest path to remote site in history.
            if (msg.getHops() < history.forwardHops) { 
                history.forwardHops = msg.getHops();
                history.forwardChannel = channelIn;
            }
            // Is message a new information about the site?
            if (msg.isNewerThan(oldMsg)) {
                // Is moving to new position?
                boolean notMoving = oldSite.getPos().equals(from.getPos());
                if (msg.getType() == Msg.MsgType.MOVEMENT && notMoving)
                    // Moving to same position is not really novelty!
                    return null;
                // Is announce this presence in other place?
                if (msg.getType() == Msg.MsgType.PRESENCE && !notMoving)
                    // This is a movement event!
                    msg.setType(Msg.MsgType.MOVEMENT); 
                // Register the news and returns the history of site. 
                history.lastMsg = msg;
                return oldMsg;
            } 
            // Old news!
            return null;
        }
        // Is it unknown site moving? 
        if (msg.getType().equals(Msg.MsgType.MOVEMENT)) 
            // Hello you!
            msg.setType(Msg.MsgType.PRESENCE);
        // Is unknown site and it's leaving?
        else if (msg.getType().equals(Msg.MsgType.ABSENCE))
            // Keep unknown!
            return null;
        // Register the news. 
        // This kind of news is the whole history of the site!
        RemoteSiteHistory history = new RemoteSiteHistory();
        history.lastMsg = msg;
        history.forwardHops = msg.getHops();
        history.forwardChannel = channelIn;
        localHistory.put(from, history);
        return msg;
    }
}
