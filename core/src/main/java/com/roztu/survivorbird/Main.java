package com.roztu.survivorbird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;

import java.util.*;

public class Main extends ApplicationAdapter {

    SpriteBatch batch;
    Texture background, bird, ufo;
    float birdX, birdY;
    int gameState = 0;
    float velocity = 0;
    float gravity = 0.33f;
    float enemyVelocity = 5;
    Random random;
    int score = 0;
    int scoredEnemy = 0;
    BitmapFont font, font2;

    Circle birdCircle;
    ShapeRenderer shapeRenderer;

    int numberOfEnemies = 4;
    float[] enemyX;
    float[][] enemyY;
    float distance;

    Circle[][] enemyCircles;

    static class Particle {
        float x, y, vx, vy, life, maxLife, size;

        public Particle(float x, float y, Random random) {
            this.x = x;
            this.y = y;
            this.maxLife = 0.8f;
            this.life = maxLife;
            this.size = 6f;

            float angle = random.nextFloat() * 2 * (float) Math.PI;
            float speed = 80f + random.nextFloat() * 150f;
            this.vx = (float) Math.cos(angle) * speed;
            this.vy = (float) Math.sin(angle) * speed;
        }

        public void update(float deltaTime) {
            x += vx * deltaTime;
            y += vy * deltaTime;
            life -= deltaTime;
            size *= 0.97f;
        }

        public boolean isDead() {
            return life <= 0;
        }
    }

    List<Particle> particles = new ArrayList<>();

    private void generateEnemyY(int index) {
        float minY = 100;
        float maxY = Gdx.graphics.getHeight() - 200;

        // Dinamik boşluk: skor arttıkça daralır
        float minGap = Math.max(100, 200 - score * 5);

        int formationType = random.nextInt(3);

        if (formationType == 0) {
            // Tekli
            enemyY[index][0] = minY + random.nextFloat() * (maxY - minY);
            enemyY[index][1] = -1000;
            enemyY[index][2] = -1000;

        } else if (formationType == 1) {
            // İkili
            float baseY = minY + random.nextFloat() * (maxY - minY - minGap);
            enemyY[index][0] = baseY;
            enemyY[index][1] = baseY + minGap + random.nextFloat() * 100;
            enemyY[index][2] = -1000;

        } else {
            // Üçlü
            float baseY = minY + random.nextFloat() * (maxY - minY - 2 * minGap);
            enemyY[index][0] = baseY;
            enemyY[index][1] = baseY + minGap + random.nextFloat() * 50;
            enemyY[index][2] = enemyY[index][1] + minGap + random.nextFloat() * 50;
        }
    }

    private void createExplosion(float x, float y) {
        for (int i = 0; i < 12; i++) {
            particles.add(new Particle(x, y, random));
        }
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        background = new Texture("background.png");
        bird = new Texture("bird.png");
        ufo = new Texture("ufo.png");

        distance = Gdx.graphics.getWidth() / 2f;
        random = new Random();

        birdX = Gdx.graphics.getWidth() / 2f - bird.getHeight() / 2f;
        birdY = Gdx.graphics.getHeight() / 3f;

        shapeRenderer = new ShapeRenderer();
        birdCircle = new Circle();

        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(4);

        font2 = new BitmapFont();
        font2.setColor(Color.WHITE);
        font2.getData().setScale(6);

        enemyX = new float[numberOfEnemies];
        enemyY = new float[numberOfEnemies][3];
        enemyCircles = new Circle[numberOfEnemies][3];

        for (int i = 0; i < numberOfEnemies; i++) {
            enemyX[i] = Gdx.graphics.getWidth() + i * distance;
            generateEnemyY(i);
            for (int j = 0; j < 3; j++) enemyCircles[i][j] = new Circle();
        }
    }

    @Override
    public void render() {
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if (gameState == 1) {
            enemyVelocity = Math.min(5 + score * 0.5f, 20f);

            float deltaTime = Gdx.graphics.getDeltaTime();
            for (int i = particles.size() - 1; i >= 0; i--) {
                Particle p = particles.get(i);
                p.update(deltaTime);
                if (p.isDead()) particles.remove(i);
            }

            if (enemyX[scoredEnemy] < birdX) {
                score++;
                scoredEnemy = (scoredEnemy + 1) % numberOfEnemies;
            }

            if (Gdx.input.justTouched()) velocity = -7;

            for (int i = 0; i < numberOfEnemies; i++) {
                if (enemyX[i] < -ufo.getWidth()) {
                    enemyX[i] += numberOfEnemies * distance;
                    generateEnemyY(i);
                } else {
                    enemyX[i] -= enemyVelocity;
                }

                for (int j = 0; j < 3; j++) {
                    if (enemyY[i][j] == -1000) continue;
                    float width = Gdx.graphics.getWidth() / 15f;
                    float height = Gdx.graphics.getHeight() / 10f;
                    float y = enemyY[i][j];

                    batch.draw(ufo, enemyX[i], y, width, height);
                    enemyCircles[i][j].set(enemyX[i] + width / 2f, y + height / 2f, width / 2f);
                }
            }

            if (birdY > 0) {
                velocity += gravity;
                birdY -= velocity;
            } else {
                gameState = 2;
            }

        } else if (gameState == 0) {
            if (Gdx.input.justTouched()) gameState = 1;

        } else if (gameState == 2) {
            font2.draw(batch, "Game Over! Tap To Play Again!", 650, Gdx.graphics.getHeight() / 2f);
            font2.setColor(Color.PINK);

            if (Gdx.input.justTouched()) {
                gameState = 1;
                birdY = Gdx.graphics.getHeight() / 3f;
                for (int i = 0; i < numberOfEnemies; i++) {
                    enemyX[i] = Gdx.graphics.getWidth() + i * distance;
                    generateEnemyY(i);
                }
                velocity = 0;
                scoredEnemy = 0;
                score = 0;
                particles.clear();
            }
        }

        batch.draw(bird, birdX, birdY, Gdx.graphics.getWidth() / 15f, Gdx.graphics.getHeight() / 10f);
        font.draw(batch, String.valueOf(score), 100, 200);
        batch.end();

        birdCircle.set(birdX + Gdx.graphics.getWidth() / 30f, birdY + Gdx.graphics.getHeight() / 20f, Gdx.graphics.getWidth() / 55f);

        for (int i = 0; i < numberOfEnemies; i++) {
            for (int j = 0; j < 3; j++) {
                if (enemyY[i][j] == -1000) continue;
                if (Intersector.overlaps(birdCircle, enemyCircles[i][j])) {
                    createExplosion(birdCircle.x, birdCircle.y);
                    gameState = 2;
                }
            }
        }

        if (!particles.isEmpty()) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (Particle p : particles) {
                float alpha = p.life / p.maxLife;
                shapeRenderer.setColor(1f, 0.4f, 0f, alpha);
                shapeRenderer.circle(p.x, p.y, p.size);
            }
            shapeRenderer.end();
        }
    }

    @Override
    public void dispose() {
    }
}
