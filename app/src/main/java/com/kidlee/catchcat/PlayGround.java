package com.kidlee.catchcat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by kidlee on 2016/1/19.
 */
public class PlayGround extends SurfaceView implements View.OnTouchListener {

    private static int WIDTH;
    private static final int ROW = 10;
    private static final int COL = 10;
    private static final int BLOCKS = 10;

    private Dot cat;
    private Dot map[][] = new Dot[ROW][COL];

    public PlayGround(Context context) {
        super(context);
        getHolder().addCallback(callback);
        for (int i=0; i<ROW; i++){
            for (int j=0; j<COL; j++){
                map[i][j] = new Dot(j, i);
            }
        }
        setOnTouchListener(this);
        initGame();
    }

    private void redraw(){
        Canvas c = getHolder().lockCanvas();
        c.drawColor(Color.LTGRAY);
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);

        for (int i=0; i<ROW; i++){

            int offset = 0;
            if(i%2 != 0){
                offset = WIDTH/2;
            }

            for (int j=0; j<COL; j++){
                Dot one = getDot(j, i);
                switch (one.getStatus()){
                    case Dot.STATUS_ON:
                        paint.setColor(0xFF8d88a1);
                        break;
                    case Dot.STATUS_OFF:
                        paint.setColor(0xFF7ad0e2);
                        break;
                    case Dot.STATUS_IN:
                        paint.setColor(0xFFff869c);
                        break;
                }
                c.drawOval(new RectF(one.getX()*WIDTH+offset+WIDTH/4, one.getY()*WIDTH,
                        (one.getX()+1)*WIDTH+offset+WIDTH/4, (one.getY()+1)*WIDTH), paint);
            }
        }

        getHolder().unlockCanvasAndPost(c);
    }

    SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            redraw();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            WIDTH = width/(COL+1);
            redraw();

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    private Dot getDot(int x, int y){
        return map[y][x];
    };

    private void initGame(){
        for (int i=0; i<ROW; i++){
            for(int j=0; j<COL; j++){
                map[i][j].setStatus(Dot.STATUS_OFF);
            }
        }

        cat = new Dot(4, 5);
        getDot(4, 5).setStatus(Dot.STATUS_IN);
        for (int i=0; i<BLOCKS; ){
            int x = (int)((Math.random()*100)%ROW);
            int y = (int)((Math.random()*100)%COL);
            if(getDot(x, y).getStatus() == Dot.STATUS_OFF){
                getDot(x, y).setStatus(Dot.STATUS_ON);
                i++;
                System.out.println(i);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP){
            int x,y;
            y = (int)(event.getY()/WIDTH);
            if(y%2 == 0){
                x = (int)((event.getX()-WIDTH/2)/WIDTH);
            }else {
                x = (int)((event.getX()-WIDTH)/WIDTH);
            }
            if(x+1>COL || y+1>ROW){
                Toast.makeText(getContext(), "不要瞎几把乱点", Toast.LENGTH_SHORT).show();
            }else {
                if(getDot(x, y).getStatus() == Dot.STATUS_OFF){
                    getDot(x, y).setStatus(Dot.STATUS_ON);
                    move();
                }
            }
            redraw();
        }
        return true;
    }

    private boolean isAtEdeg(Dot dot){
        if(dot.getX()*dot.getY() == 0 || dot.getX()+1 == COL || dot.getY()+1 == ROW){
            return true;
        }
        return false;
    }

    private Dot getneighbor(Dot dot, int dir){
        switch (dir){
            case 1:
                return getDot(dot.getX()-1,dot.getY());
            case 2:
                if(dot.getY()%2 == 0){
                    return getDot(dot.getX()-1,dot.getY()-1);
                }else {
                    return getDot(dot.getX(),dot.getY()-1);
                }
            case 3:
                if(dot.getY()%2 == 0){
                    return getDot(dot.getX(),dot.getY()-1);
                }else {
                    return getDot(dot.getX()+1,dot.getY()-1);
                }
            case 4:
                return getDot(dot.getX()+1,dot.getY());
            case 5:
                if(dot.getY()%2 == 0){
                    return getDot(dot.getX(),dot.getY()+1);
                }else {
                    return getDot(dot.getX()+1,dot.getY()+1);
                }
            case 6:
                if(dot.getY()%2 == 0){
                    return getDot(dot.getX()-1,dot.getY()+1);
                }else {
                    return getDot(dot.getX(),dot.getY()+1);
                }
        }
        return null;
    }

    private int getDistance(Dot dot, int dir){
        Dot ori = dot,next;
        int distance = 0;
        if (isAtEdeg(dot)){
            return 1;
        }
        while (true){
            next = getneighbor(ori,dir);
            if(next.getStatus() == Dot.STATUS_ON){
                return distance*-1;
            }
            if(isAtEdeg(next)){
                distance++;
                return distance;
            }
            distance++;
            ori = next;
        }
    }

    private void moveTo(Dot dot){
        dot.setStatus(Dot.STATUS_IN);
        getDot(cat.getX(),cat.getY()).setStatus(Dot.STATUS_OFF);
        cat.setXY(dot.getX(), dot.getY());
    }

    private void move() {
        if (isAtEdeg(cat)) {
            lose();
            return;
        }
        Vector<Dot> base = new Vector<>();
        Vector<Dot> positive = new Vector<>();
        HashMap<Dot, Integer> al = new HashMap<Dot, Integer>();

        for (int i = 1; i < 7; i++) {
            Dot n = getneighbor(cat, i);
            if (n.getStatus() == Dot.STATUS_OFF) {
                base.add(n);
                al.put(n, i);
                if (getDistance(n, i) > 0) {
                    positive.add(n);
                }
            }
        }

        if (base.size() == 0) {
            win();
        } else if (base.size() == 1) {
            moveTo(base.get(0));
        } else {
            Dot best = null;
            if (positive.size() != 0) {//能到达边界
                System.out.println("前进");
                int min = 100;
                for (int i = 0; i < positive.size(); i++) {
                    int a = getDistance(positive.get(i), al.get(positive.get(i)));
                    if (a < min) {
                        min = a;
                        best = positive.get(i);
                    }
                }
            } else {//没有到达边界的路;
                System.out.println("躲避");
                System.out.println(base.size());
                int max = 0;
                for (int i = 0; i < base.size(); i++) {
                    int k = getDistance(base.get(i), al.get(base.get(i)));
                    System.out.println(k);
                    if (k <= max) {
                        max = k;
                        best = base.get(i);
                    }
                }
            }
            moveTo(best);
        }
    }

    private void lose(){
        Toast.makeText(getContext(), "你输了~", Toast.LENGTH_SHORT).show();
        initGame();
    }

    private void win(){
        Toast.makeText(getContext(), "你赢了~", Toast.LENGTH_SHORT).show();
        initGame();
    }
}

