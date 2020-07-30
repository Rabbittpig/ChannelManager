package com.example.channelmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String CHANNEL_DATA_FILE = "channel_data.json";
    private List<String> mUserList = new ArrayList<>();
    private List<String> mOtherList = new ArrayList<>();
    //声明两个GridView,分别是我的频道和其他频道
    private GridView mUserGv;
    private GridView mOtherGv;

    //声明两个Adapter,分别对应两个不同的GridView
    private ChannelAdapter mUserAdapter;
    private ChannelAdapter mOtherAdapter;

    private TextView mMore;
    /**
     * 动画时长为300毫秒
     */
    private int ANIM_DURATION = 300;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        // 1、获取所有的控件
        // 获取"用户频道"更多频道"得GridView
        mUserGv = findViewById(R.id.user_gridView);
        mOtherGv = findViewById(R.id.other_gridView);
        mMore = findViewById(R.id.tv_more);

        // 2、初始化数据,并装载数据

        try {
            InputStream is = getAssets().open(CHANNEL_DATA_FILE);
            int length = is.available();
            byte[] buffer = new byte[length];
            is.read(buffer);
            String result = new String(buffer, "utf-8");
            JSONObject jsonObject = new JSONObject(result);
            JSONArray userArray = jsonObject.optJSONArray("user");
            JSONArray otherArray = jsonObject.optJSONArray("other");

            //循环遍历json文件里的"user"数组,读取到userList当中
            for (int i = 0; i < userArray.length(); i++) {
                mUserList.add(userArray.optString(i));
            }
            //循环遍历 装载otherList
            for (int i = 0; i < otherArray.length(); i++) {
                mOtherList.add(otherArray.optString(i));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //3、初始化适配器,并绑定适配器
        mUserAdapter = new ChannelAdapter(this, mUserList, true);
        mOtherAdapter = new ChannelAdapter(this, mOtherList, false);
        mUserGv.setAdapter(mUserAdapter);
        mOtherGv.setAdapter(mOtherAdapter);

        //4.设置"编辑"Button,GridView的点击事件
        mUserGv.setOnItemClickListener(this);
        mOtherGv.setOnItemClickListener(this);


        findViewById(R.id.iv_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleEditState();
                ((ImageView) view).setImageResource(ChannelAdapter.isEdit() ? R.mipmap.ok : R.drawable.edit);
            }
        });
    }

    /**
     * 切换编辑态和非编辑态
     */
    private void toggleEditState() {
        boolean isEdit = ChannelAdapter.isEdit();
        ChannelAdapter.setEdit(!isEdit);
        mMore.setVisibility(isEdit ? View.INVISIBLE : View.VISIBLE);
        mOtherGv.setVisibility(isEdit ? View.INVISIBLE : View.VISIBLE);
        mUserAdapter.notifyDataSetChanged();
        mOtherAdapter.notifyDataSetChanged();
    }

    /**
     * 移动动画
     *
     * @param moveView 移动的目标View
     * @param startPos 起点坐标
     * @param endPos   终点坐标
     * @param duration 动画时长
     */
    private void moveAnimation(final View moveView, int[] startPos, int[] endPos, int duration) {
        //1.创建动画,设置起点和终点坐标
        TranslateAnimation animation = new TranslateAnimation(startPos[0], endPos[0], startPos[1], endPos[1]);
        //2.设置动画时长
        animation.setDuration(duration);
        //3.设置不停留
        animation.setFillAfter(false);
        //4.设置监听器
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //动画结束,移除cloneView
                ((ViewGroup)moveView.getParent()).removeView(moveView);
              resetAdapter();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        //5.启动动画
        moveView.startAnimation(animation);
    }

    private void resetAdapter() {
        mUserAdapter.setTranslating(false);
        mOtherAdapter.setTranslating(false);

        mUserAdapter.remove();
        mOtherAdapter.remove();
    }

    private ImageView getCloneView(View view) {
        //旧版本API,当前已弃用
      /*  view.destroyDrawingCache();
        view.setDrawingCacheEnabled(true);
        Bitmap cache = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(cache);*/
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        return imageView;

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //判断是否为编辑态
        //如果是编辑态则点击的时候处理删除/增加操作
        //如果不是编辑态则弹出Toast提示,实际使用中可以换成频道详情页跳转
        if(ChannelAdapter.isEdit()){
            GridView currentView;
            final GridView anotherView;
            //currentView表示当前被点击的GridView对象,anotherView表示另一个GridView
            //这里统一定义current和another,方便后续无论点击的是哪一个均可复用同一套逻辑
            if(adapterView == mUserGv){
                currentView = mUserGv;
                anotherView = mOtherGv;
            }else{
                currentView = mOtherGv;
                anotherView = mUserGv;
            }
            //计算起点,获取点击view的坐标
            final int[] startPos = new int[2];
            final int[] endPos = new int[2];
            view.getLocationInWindow(startPos);
            ChannelAdapter currentAdapter = (ChannelAdapter) currentView.getAdapter();
            ChannelAdapter anotherAdapter = (ChannelAdapter) anotherView.getAdapter();
            //标记点击的GridView item待删除,并添加到anotherView中
            anotherAdapter.setTranslating(true);
            anotherAdapter.add( currentAdapter.setRemove(i));////////////////////////position替换i

            final ImageView cloneView = getCloneView(view);
            ((ViewGroup)getWindow().getDecorView())
                    .addView(cloneView,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT );


            currentView.post(new Runnable() {
                @Override
                public void run() {
                    View lastView = anotherView.getChildAt(anotherView.getChildCount() - 1);
                    lastView.getLocationInWindow(endPos);
                    moveAnimation(cloneView,startPos,endPos,ANIM_DURATION);
                }
            });

        }else {
            //频道入口
            Toast.makeText(this,
                    mUserList.get(i)+"频道入口",Toast.LENGTH_SHORT).show();
        }
    }
}