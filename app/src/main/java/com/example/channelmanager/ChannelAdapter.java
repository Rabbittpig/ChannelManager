package com.example.channelmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class ChannelAdapter extends BaseAdapter {
    /**
     * 标识编辑态
     */
    private static boolean mInEditState;
    private Context mContext;
    private boolean mIsUserChannel;
    private AnimState mAnimState = AnimState.IDLE;
    /**
     * 数据列表
     */
    private List<String> mList;
    /**
     * 标记预备删除的元素序号
     */
    private int mReadyToRemove = -1;

    /**
     * 动画状态枚举,用于对不同动画状态进行处理,当前只支持空闲和移动.
     * 目前看也可以用boolean,enum便于扩展
     */
    enum AnimState{
        IDLE,
        TRANSLATIHNG
    }

    /**
     * 构造器,需要强制传入上下文和数据列表以及是否是用户频道
     *
     * @param context  上下文
     * @param list  数据列表
     *  @param isUser 是否是用户频道
     */
    public ChannelAdapter(Context context, List<String> list , boolean isUser){
        mContext = context;
        mList = list;
        mIsUserChannel =isUser;
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    //关键函数!!!
    //实现ui和数据的绑定关系
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        //1.复用view,只创建一次,节省性能
        if (view ==null) {
            view = LayoutInflater
                    .from(mContext).inflate(R.layout.channel_item, null);
        }

        //2.绑定viewHolder,避免频繁的findViewById耗时
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        if(viewHolder ==null){
            viewHolder = new ViewHolder();
            viewHolder.iv = view.findViewById(R.id.iv_icon);
            viewHolder.tv = view.findViewById(R.id.tv_item);
            view.setTag(viewHolder);
        }
        //3.获取item控件对象
        TextView tv = viewHolder.tv;
        ImageView iv = viewHolder.iv;


        //4.编写业务逻辑

        //根据当前是否为编辑态,来决定是否显示编辑icon,(显示加或者减)
        if(mInEditState){
            iv.setVisibility(View.VISIBLE);
            iv.setImageResource(mIsUserChannel?R.mipmap.jian :R.drawable.add);
        }else{
            iv.setVisibility(View.INVISIBLE);
        }
        //1.第一个条件处理currentView的状态
        //2.第二个条件处理anotherView的状态

        if(mReadyToRemove == i
                || mAnimState == AnimState.TRANSLATIHNG && i == getCount() - 1){
        tv.setText("");
        tv.setSelected(true);
        iv.setVisibility(View.INVISIBLE);
        }else{

            tv.setText(mList.get(i));
            tv.setSelected(false);
        }

        return view;
    }
    /**
     *添加列表数据,并触发列表刷新
     * @param channelName 频道的文本
     */
    public void add(String channelName){
        mList.add(channelName);
        notifyDataSetChanged();
    }

    /**
     * 添加删除标记
     * @param index 待删除的序号
     */

    public String setRemove(int index){
        mReadyToRemove = index;
        notifyDataSetChanged();
        return mList.get(index);
    }

    public void remove(){
        remove(mReadyToRemove);
        mReadyToRemove = -1;
    }
    void setTranslating(boolean translating){
        mAnimState = translating ? AnimState.TRANSLATIHNG : AnimState.IDLE;

    }

    /**
     * 删除指定的列表序号,并刷新列表
     * @param index 待删除的序号
     */
    public void remove(int index){
        if(index > 0 &&index<mList.size()){
            mList.remove(index);
        }
        notifyDataSetChanged();
    }

    /**
     * 设置当前是否是编辑态
     * @param isEdit true:编辑态 false:非编辑态
     */
    static  void setEdit(boolean isEdit){
       mInEditState = isEdit;
    }

    /**
     * 判断当前是否是编辑态
     * @return true:编辑态 false:非编辑态
     */
    static  boolean isEdit(){
        return mInEditState;
    }
    private class ViewHolder{
        TextView  tv;
        ImageView iv;

    }
}
