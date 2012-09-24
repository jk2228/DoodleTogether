package com.jisha.DoodleTogether;

import java.io.Serializable;

import android.graphics.Path;
import android.util.Log;

public class MyPath extends Path implements Serializable {
	public Path src= new Path();//testing
	 private static final long serialVersionUID = 1L;
	 
	 public MyPath(){
		 super();
	 }
	 
//	 @Override
//	 public void lineTo(float x, float y){
//		 super.lineTo(x, y);
//		 try {
//			src=(Path)super.clone();
//			if (src.isEmpty()){
//				Log.d("path","src is empty after executing lineTo");
//			}
//		} catch (CloneNotSupportedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	 }
//	 
//	 public void moveTo(float x, float y){
//	 super.lineTo(x, y);
//	 try {
//		src=(Path)super.clone();
//		if (src.isEmpty()){
//			Log.d("path","src is empty after executing lineTo");
//		}
//	} catch (CloneNotSupportedException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	 }
//	 
//	 public void quadTo (float x1, float y1, float x2, float y2){
//		 super.quadTo(x1,y1,x2,y2);
//		 if (src.isEmpty()){
//				Log.d("path","src is empty after executing lineTo");
//			}
//		 try {
//			src=(Path)super.clone();
//		} catch (CloneNotSupportedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		 }
}
