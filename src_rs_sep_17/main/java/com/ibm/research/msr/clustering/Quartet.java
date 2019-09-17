package com.ibm.research.msr.clustering;

import java.util.ArrayList;
import java.util.List;


class Quartet{
	double size;
	int i;
	public double getSize() {
		return size;
	}
	public void setSize(double size) {
		this.size = size;
	}
	public int getI() {
		return i;
	}
	public void setI(int i) {
		this.i = i;
	}
	public int getJ() {
		return j;
	}
	public void setJ(int j) {
		this.j = j;
	}
	public List<ClusterDetails> getCd() {
		return cd;
	}
	public void setCd(ArrayList<ClusterDetails> cd) {
		this.cd = cd;
	}
	int j;
	List<ClusterDetails> cd;
	public Quartet(double size, int i, int j, List<ClusterDetails> cd) {
		super();
		this.size = size;
		this.i = i;
		this.j = j;
		this.cd = cd;
	}
//	public Quartet(double sz, int i2, int j2, List<ClusterDetails> newcls) {
//		// TODO Auto-generated constructor stub
//		super();
//		this.size = size;
//		this.i = i;
//		this.j = j;
//		this.cd = cd;
//	}
	
	
}