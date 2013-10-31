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
import daj.Message;

// ----------------------------------------------------------------------------
//
// a message class
//
// ----------------------------------------------------------------------------
class Msg extends Message {

    public enum MsgType {
        PRESENCE, 
        ABSENCE,
        MOVEMENT
    }
    
    private int deep;
    private MsgType type;
    private Site from;
    private int remainingHops;
    private int localTime;
    private Site to = null;
    
    public Msg (Msg msg) {
        this.deep = msg.deep;
        this.type = msg.type;
        this.from = new Site(msg.from);
        this.remainingHops = msg.remainingHops;
        this.localTime = msg.localTime;
        if (msg.to != null)
            this.to = new Site(msg.to);
    }
    
    public Msg (Site site, int time, MsgType type, int deep) {
        this.from = new Site(site);
        this.type = type;
        this.deep = deep;
        this.remainingHops = deep;
        this.localTime = time;
    }
    
    public Msg (Site site, int time, MsgType type, Site to, int deep) {
        this(site, time, type, deep);
        this.to = new Site(to);
    }
    
    public MsgType getType() {
        return type;
    }
    
    public void setType(MsgType type) {
        this.type = type;
    }
    
    public Site getFrom() {
        return from;
    }
    
    public String getText() {
        return this.toString();
    }
    
    public boolean isAlive() {
        return this.remainingHops > 0;
    }
    
    public boolean isBroadcast() {
        return this.to == null;
    }
    
    public boolean isUnicast() {
        return this.to != null;
    }
    
    public boolean isTo(Site to) {
        if (this.to != null)
            return this.to.equals(to);
        return false;
    }
    
    public void consume() {
        this.remainingHops = 0;
    }
    
    public void nextHop() {
        --this.remainingHops;
    }
    
    public int getHops() {
        return this.deep - this.remainingHops;
    }
    
    public int getTime() {
        return this.localTime;
    }
    
    public boolean isNewerThan(Msg msg) {
        return this.localTime > msg.localTime;
    }
    
    public Site getTo() {
        return to;
    }
    
    @Override
    public String toString() {
        return "Msg [type=" + type + " from=" + from + " to=" + to + 
                " hops=" + getHops() + "]";
    }
    
}

