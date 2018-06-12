package de.aitools.aq.wat.io;

public enum Client {
  ANNOTATOR {
    @Override
    public boolean isAdmin() {
      return false;
    }
  },
  ADMIN {
    @Override
    public boolean isAdmin() {
      return true;
    }
  };
  
  public abstract boolean isAdmin();
}
