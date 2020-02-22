package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Vector;

public class MyGdxGame extends ApplicationAdapter {
    private Texture dropImage;
    private Texture bucketImage;
    private Sound dropSound;
    private Music rainMusic;

    private OrthographicCamera camera; //для р
    private SpriteBatch batch; //для отрисовки 2д изображений

    private Rectangle bucket;
    private Array<Rectangle> rainDrops;
    private long lastDropItem;

    private void spawnRainDrops() {
        Rectangle rainDrop = new Rectangle();
        rainDrop.x = MathUtils.random(0, 800 - 64);
        rainDrop.y = 480;
        rainDrop.width = 64;
        rainDrop.height = 64;
        rainDrops.add(rainDrop);
        lastDropItem = TimeUtils.nanoTime();

    }

    @Override
    public void create() {
        super.create();

        dropImage = new Texture(Gdx.files.internal("droplet.png"));
        bucketImage = new Texture(Gdx.files.internal("bucket.png"));
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

        rainMusic.setLooping(true);
        rainMusic.play();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        batch = new SpriteBatch();

        bucket = new Rectangle();
        bucket.x = 800 / 2 - 64 / 2;
        bucket.y = 20;
        bucket.height = 64;
        bucket.width = 64;

        rainDrops = new Array<Rectangle>();
        spawnRainDrops();

    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //бновление камеры
        camera.update();

        //указываем SpriteBatch координаты системы для камеры
        batch.setProjectionMatrix(camera.combined);

        //отрисовка ведра
        batch.begin();
        batch.draw(bucketImage, bucket.x, bucket.y);
        for (Rectangle rainDrop : rainDrops) {
            batch.draw(dropImage, rainDrop.x, rainDrop.y);
        }
        batch.end();

        //перемещение карзины
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos); //чтобы клик по экрана рассчитывался в пределах viewport'a (ширина и высота экрана)
            bucket.x = (int) touchPos.x - 64 / 2;
        }
        //перемещение на стрелки
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucket.x -= 500 * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucket.x += 555 * Gdx.graphics.getDeltaTime();
        }

        if (bucket.x < 0) {
            bucket.x = 0;
        }
        if (bucket.x > 800 - 64) {
            bucket.x = 800 - 64;
        }
        //проверяем сколько секунд прошло после последней капли
        if (TimeUtils.nanoTime() - lastDropItem > 1000000000) {
            spawnRainDrops();
        }

        //падение капель, удаление капель, воспроизведение звука при подении капель
        for (Iterator<Rectangle> iter = rainDrops.iterator(); iter.hasNext(); ) {
            Rectangle rainDrop_loop = iter.next();
            rainDrop_loop.y -= 200 * Gdx.graphics.getDeltaTime();

            //как только капля поподает за нижнюю границу она удоляется
            if (rainDrop_loop.y + 64 < 0) {
                iter.remove();
            }
            if (rainDrop_loop.overlaps(bucket)) {
                dropSound.play();
                iter.remove();
            }
        }
    }
    @Override
    public void dispose(){
        dropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
        batch.dispose();
    }

}