package org.java.courses.leaderus;
public class QuickSort {

	private static final int[] arr = new int[]{12,7, 11, 90, 19, 23, 89, 17, 5,16, 43, 21, 51,55 };
	
    public static void main(String[] args) {
    	System.out.println("寻找轴点之前：");
    	printArr(arr);
    	int pivot = createPivot(arr,0,arr.length-1);
    	System.out.println("\n轴点："+pivot);
    	System.out.println("寻找轴点之后：");
    	printArr(arr);
    }

    public static void printArr(int[] arr){
    	for(int i=0;i<arr.length;i++){
    		System.out.print(arr[i]+",");
    	}
    }
    
    public static void sort(){
        
    }
    
    public static int createPivot(int[] s,int l,int h){
    	int pivot = s[l];
    	while(l < h){
    		while(l<h && s[l] <= pivot){
    			++l;
    		}
    		swap(h,l);
    		while(l < h && s[h] >= pivot){
    			--h;
    		}
    		swap(l,h);
    	}
    	return l;
    }
    
    public static void swap(int src,int target){
    	arr[src] = arr[src] + arr[target];
    	arr[target] = arr[src] - arr[target];
    	arr[src] = arr[src] - arr[target];
    }
    
    
}
