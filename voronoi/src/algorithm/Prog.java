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
import java.util.Random;


import daj.Program;

// ------------------------------------------------------------------------
//
// a program class
//
// ------------------------------------------------------------------------
public class Prog extends Program {
    
    private final int defaultHalfPresenceDelay = 5000;
    private final int defaultMovementDelay = 1000;
    private final int defaultCleanupDelay = 10000;
    
    private Site site;                  // Local site.
    private int eventNumber;            // Local event index
    private DistributedVoronoi diagram; // Voronoi diagram
    private LocalHistory history;       // Local history
    private Msg lastMsg;                // only to simulation display purpose
    
    // Mechanism to control transmission of periodic events.
    // Maybe, would be better to use real timer (threading and locks).
    // But, this implementation suffices. 
    private TimerControl movementTimer; 
    private TimerControl refreshTimer;
    private TimerControl cleanupTimer;
    private int halfPresenceDelay = defaultHalfPresenceDelay;
    private int movementDelay = defaultMovementDelay;
    private int cleanupDelay = defaultCleanupDelay;
    private Random random = new Random(); 

 // Static irrelevant sites cleanup
    private boolean cleanupIrrelevantSitesPeriodically = true;  
    private boolean forwardPresenceOnAdd = true;
    private boolean announcePresencePeriodically = true;
    private boolean floodOnForwardFail = true;

    // ----------------------------------------------------------------------
    // called for initialization of program
    // ----------------------------------------------------------------------
    public Prog (VoronoiDiagramFactory factory,
            Site site, boolean ignoreIrrelevantSites) {
        this.site = site;
        movementTimer = new TimerControl(0); 
        refreshTimer = new TimerControl(2*halfPresenceDelay);
        cleanupTimer = new TimerControl(cleanupDelay);
        random = new Random(); 
        // Create a Voronoi controller and visualization application. 
        diagram = new DistributedVoronoi(factory, site, ignoreIrrelevantSites);
    }
    
    // ----------------------------------------------------------------------
    // configuration parameters 
    // It is expected that the names are self-explanatory (hope to). 
    // ----------------------------------------------------------------------
    public void setCleanupIrrelevantSitesPeriodically(
            boolean cleanupIrrelevantSites) {
        this.cleanupIrrelevantSitesPeriodically = cleanupIrrelevantSites;
    }
    public void setForwardPresenceOnAdd(boolean confirmPresence) {
        this.forwardPresenceOnAdd = confirmPresence;
    }
    public void setAnnouncePresencePeriodically(boolean refreshPresence) {
        this.announcePresencePeriodically = refreshPresence;
    }

    public void setFloodOnForwardFail(boolean floodOnForwardFail) {
        this.floodOnForwardFail = floodOnForwardFail;
    }

    public void setfPresenceDelay(int presenceDelay) {
        this.halfPresenceDelay = presenceDelay / 2;
    }

    public void setMovementDelay(int movementDelay) {
        this.movementDelay = movementDelay;
    }

    public void setCleanupDelay(int cleanupDelay) {
        this.cleanupDelay = cleanupDelay;
    }

    // ----------------------------------------------------------------------
    // called for execution of program
    // ----------------------------------------------------------------------
    public void main() {
        //int channel;
        // start event index
        eventNumber = 0;
        // Broadcast the presence of this site (process). 
        send(Msg.MsgType.PRESENCE);
        // Create a local history
        history = new LocalHistory(site);
        // Execute indefinitely (continuous)
        try {
            while (true) {
                // Occurs self movement?
                if (adjustPosition()) 
                    // schedule the announcement (next delay) 
                    movementTimer.on(movementDelay);
                // Occurs movement and past enough time since last message?
                if (movementTimer.passTime()) {
                    // Notify others to update the location of this process
                    movementTimer.off();
                    refreshTimer.restart();
                    send(Msg.MsgType.MOVEMENT);
                } else
                    // It's time to periodic self announcement?
                    if (announcePresencePeriodically && 
                            refreshTimer.passTime()) {
                        // Notify others to update to a more accurate state.
                        // This event try to compensate limitations of the 
                        // algorithm and topology like local memory cleanup, 
                        // unknown movements or others.
                        refreshTimer.on(halfPresenceDelay +
                                random.nextInt(halfPresenceDelay));
                        send(Msg.MsgType.PRESENCE);
                    } else 
                        // It's time to clear irrelevant sites?
                        if (cleanupIrrelevantSitesPeriodically && 
                                cleanupTimer.passTime()){
                            cleanupTimer.on(cleanupDelay);
                            clean();
                        }
                // Gets the channel (FIFO) with next message (if there), 
                // without block the execution.
                // Here, there are many strategies to read messages 
                // from channel buffers.
                // Strategy: Take a complete round over all input channels.
                // In case of multiple link per site, it tend to give 
                // a better chance to each remote site to communicate.
                for (int c = 0; c < in().getSize(); c++) 
                    processMessage(getMessage(c), c);
            }
        } finally {
            // Before exit, announced its intention
            send(Msg.MsgType.ABSENCE);
        }
    }

    /*
     * Read the site position in simulator and adjust in Voronoi controller 
     */
    private boolean adjustPosition() {
        int x = node.getVisual().x();
        int y = node.getVisual().y();
        if (x != site.getPos().getX() || y != site.getPos().getY()) {
            diagram.moveLocal(x, y);
            return true;
        }
        return false;
    }

    /*
     * Remove a message from channel buffer and adjust local event index.
     */ 
    private Msg getMessage(int index) {
        Msg in = (Msg)(in(index).receive(1));
        if (in != null) {
            ++eventNumber;
            in = new Msg(in);
            lastMsg = in;
        }
        return in;
    }

    /*
     * Create message with current site information and send to all channels.
     */ 
    private Msg send(Msg.MsgType msgType) {
        Msg out = new Msg(site, ++eventNumber, msgType);
        out().send(out);
        refreshTimer.restart();
        return out;
    }

    /*
     * Create message with current site information and forward it.
     */ 
    private Msg send(Msg.MsgType msgType, Site to, int through) {
        if (through >= 0 && through < out().getSize()) { 
            Msg out = new Msg(site, ++eventNumber, msgType, to);
            out(through).send(out);
            return out;
        }
        return null;
    }
    
    private void clean() {
        ArrayList<Site> sites = diagram.delIrrelevantSites();
        history.clean(sites);
        cleanupTimer.restart();
    }
    
    private boolean addNewSiteToDiagram(Msg in) {
        // Is remote site new and relevant ?
        if (diagram.addRemote(in.getFrom())) {
            // Everybody must have a chance to know 
            // about this too, at least the remote site.
            if (forwardPresenceOnAdd && in.isBroadcast())
                send(Msg.MsgType.PRESENCE, in.getFrom(), 
                        history.forwardChannel(in.getFrom()));
            return true;
        }
        return false;
    }
    
    private boolean processMessage(Msg in, int channel) {
        // No message, no process...
        if (in == null)
            return false;
        // If input message has a news...
        if (history.register(in, channel)) {
            // In response, change the local state according:
            switch(in.getType()) {
            case PRESENCE:
            case MOVEMENT:
                addNewSiteToDiagram(in);
                break;
            case ABSENCE:
                diagram.delRemote(in.getFrom());
                break;
            default:
                break;
            }
            // Try to retransmit the news.
            retransmitMessage(in, channel);
        }
        return true;
    }

    private void retransmitMessage(Msg in, int inChannel) {
        // The message is broadcasting?
        if (in.isBroadcast()) {
            // Try to retransmit the message
            flood(in, inChannel);
           
        } else // The message is unicast!
            // The message need to be forwarded (is it not to this site)?
            if (!in.isTo(site)){
                // Then, try to do forwarding.
                // If can't forward or probable path do not exists... 
                if (!forward(in, history.forwardChannel(in.getTo())))
                    // then, ignore or flooding.
                    if (floodOnForwardFail)
                        flood(in, inChannel);
            }
    }
    
    
    /*
    * Any distributed  algorithm depends on topology.
    * In this case (simulator), the communication model 
    * (based on fixed and predefined directional channels) 
    * is independent from location,  but the problem 
    * (algorithmic solution)  is not. 
    * The communication model to deal with this mismatch 
    * is based on relay the message to all channels, 
    * except the input (flooding the network), 
    * hoping to reach relevant nodes.
    * Is necessary to limit the numbers of 
    * retransmissions to avoid infinite loop.
    */
    private void flood(Msg msg, int inChannel) {
        if (out().getSize() < 3) 
            floodInOrder(msg, inChannel);
        else
            floodRandomically(msg, inChannel);
    }
    
    private void floodInOrder(Msg msg, int inChannel) {
        // Check message depth (time to live by count hops)
        if (msg.isAlive()) {
            // Count this retransmission (hop)
            msg.nextHop();
            // send message to all channels, except one (originating)
            for(int i = 0; i < inChannel; i++) 
                out(i).send(msg);
            for(int i = (out().getSize() - 1); i > inChannel; i--) 
                out(i).send(msg);
        }
    }
    private void floodRandomically(Msg msg, int inChannel) {
        
        // Check message depth (time to live by count hops)
        if (msg.isAlive()) {
            // Count this retransmission (hop)
            msg.nextHop();
            // send message to all channels, except one (originating)
            // by random order
            int channels = out().getSize();
            boolean sent[] = new boolean[channels];
            sent[inChannel] = true;
            int remain = channels - 1;
            int randomChannel;
            while (remain > 0) {
                randomChannel = random.nextInt(channels);
                if (sent[randomChannel]) continue;
                sent[randomChannel] = true;
                remain--;
                this.out(randomChannel).send(msg);
            }
        }
    }

    private boolean forward(Msg msg, int outChannel) {
        if (outChannel >= 0 && outChannel < out().getSize()) { 
            if (msg.isAlive()) {
                // Count this retransmission (hop)
                msg.nextHop();
                // send message to output channel
                out(outChannel).send(msg);
            }
            return true;
        }
        return false;   
    }

    // ----------------------------------------------------------------------
    // called for display of program state
    // ----------------------------------------------------------------------
    public String getText() {
        return this.toString();
    }
    
    @Override
    public String toString() {
        return "Prog [site=" + site + ", msg=" + lastMsg + 
                ", count=" + eventNumber + "]";
    }
}
