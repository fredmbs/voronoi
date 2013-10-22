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

public class TimerControl {

    private long lastTime;
    private long startTime;
    private long waitTime;
    private boolean timerOff;
    
    public TimerControl(long delay) {
        lastTime = System.currentTimeMillis();
        startTime = lastTime;
        waitTime = delay;
        timerOff = (delay <= 0);
    }

    /*
     * Schedule next timer, if it is not running.
     */ 
    public void on(long delay) {
        timerOff = (delay <= 0);
        waitTime = delay;
        // if the timer already reached limit, restart it.
        if (passTime())
            startTime = System.currentTimeMillis();
    }

    /*
     * Restart timer.
     */ 
    public void restart() {
        startTime = System.currentTimeMillis();
        timerOff = false;
    }

    /*
     * Stop the timer.
     */ 
    public void off() {
        timerOff = true;
    }

    /*
     * Start the timer.
     */ 
    public void on() {
        timerOff = false;
    }

    /*
     * Is it time to execute the next periodic event?
     */ 
    public boolean passTime() {
        if (timerOff) return false;
        lastTime = System.currentTimeMillis();
        return ((lastTime - startTime) > waitTime) || 
                (lastTime < startTime);
    }
    
}
