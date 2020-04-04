package Ambassadrs;

public class Ambassador {
  private String name;
  private String major;
  private int uin;
  private double hours;


  public Ambassador(String name, String major, int uin, int hours) {
    this.name = name;
    this.major = major;
    this.uin = uin;
    this.hours = hours;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMajor() {
    return major;
  }

  public void setMajor(String major) {
    this.major = major;
  }

  public int getUin() {
    return uin;
  }

  public void setUin(int uin) {
    this.uin = uin;
  }

  public double getHours() {
    return hours;
  }

  public void setHours(double hours) {
    this.hours = hours;
  }
}
