package org.java.courses.leaderus;
public class QuickSort {

	private static final int[] arr = new int[]{12,7, 11, 90, 19, 23, 89, 17, 5,16, 43, 21, 51,55 };
	
    public static void main(String[] args) {
    	System.out.println("排序之前：");
    	System.out.println(arr.length);
    	printArr(arr);
    	sort(arr,0,arr.length-1);
    	System.out.println("排序之后：");
    	printArr(arr);
    	System.out.println(arr.length);
    }

    public static void printArr(int[] arr){
    	for(int i=0;i<arr.length;i++){
    		System.out.print(arr[i]+",");
    	}
    	System.out.println();
    }
    
    public static void sort(int[] array,int left,int right){
        if(left >= right){
        	return;
        }
        int pivot = partition(array,left,right);
        sort(array,left,pivot-1);
        sort(array,pivot+1,right);
    }
    
    public static int partition(int[] s,int l,int h){
    	int pivot = s[l];
    	int i=l;
    	int j=h;
    	while(i<j){
    		while(s[i]<pivot && i<h){
    			i++;
    		}
    		while(s[j]>pivot && j>l){
    			j--;
    		}
    		if(i!=j){
    			swapNoTmp(s,i,j);
    		}
    	}
    	return j;
    }
    public static void swapNoTmp(int[] a,int src,int target){
    	a[src] = a[src] + a[target];
    	a[target] = a[src] - a[target];
    	a[src] = a[src] - a[target];
    }
    
    public static void swapWithTmp(int[] a,int src,int target){
    	int tmp = a[src];
    	a[src]=a[target];
    	a[target]=tmp;
    }
    
   
    
    
}
