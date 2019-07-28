package de.aitools.aq.wat.io;

import java.util.Date;

public class WorkTime {
  
  private long millis;
  
  private long lastEvent;
  
  public WorkTime() {
    this.millis = 0;
    this.lastEvent = 0;
  }
  
  public synchronized void openTask(final Date date, final int timeZoneOffset) {
    this.lastEvent = date.getTime() + timeZoneOffset * 60000;
  }
  
  public synchronized void annotate(final Date date, final int timeZoneOffset) {
    final long thisEvent = date.getTime() + timeZoneOffset * 60000;
    this.millis += thisEvent - this.lastEvent;
    this.lastEvent = thisEvent;
  }
  
  public long getMilliseconds() {
    return this.millis;
  }
  
  public int getSeconds() {
    return (int) (this.getMilliseconds() / 1000);
  }
  
  public int getMinutes() {
    return this.getSeconds() / 60;
  }
  

}
