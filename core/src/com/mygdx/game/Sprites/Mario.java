package com.mygdx.game.Sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Screens.*;


public class Mario extends Sprite {
    public enum State {FALLING, JUMPING, STANDING, RUNNING}
    public State currentState;
    public State previousState;
    public World world;
    public Body b2body;
    public TextureRegion marioStand;
    private Animation marioRun;
    private Animation marioJump;
    private float stateTimer;
    private boolean runningRight;

    public Mario(PlayScreen screen){
        super(screen.getAtlas().findRegion("little_mario"));
        this.world = screen.getWorld();
        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        runningRight = true;

        Array<TextureRegion> frames = new Array<TextureRegion>();
//run
        for (int i = 1; i<4; i++){
            frames.add(new TextureRegion(getTexture(), i*16, 0, 16, 16));
        }
        marioRun = new Animation(0.1f, frames);
        frames.clear();

        for (int i = 4; i<6; i++){
            frames.add(new TextureRegion(getTexture(), i*16, 0, 16, 16));
        }
        marioJump = new Animation(0.1f, frames);

        marioStand = new TextureRegion(getTexture(), 0, 0, 16, 16);
        defineMario();

        setBounds(0, 0, 16/MyGdxGame.PPM, 16/MyGdxGame.PPM);
        setRegion(marioStand);
    }

    public void update(float dt){
        setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
        setRegion(getFrame(dt));
    }

    public TextureRegion getFrame(float dt){
        currentState = getState();

        TextureRegion region;
        switch (currentState){
            case JUMPING:
                region = (TextureRegion) marioJump.getKeyFrame(stateTimer);
                break;
            case RUNNING:
                region = (TextureRegion) marioRun.getKeyFrame(stateTimer, true);
                break;
            case FALLING:
            case STANDING:
            default:
                region = marioStand;
                break;

        }
        if ((b2body.getLinearVelocity().x<0 || !runningRight) && !region.isFlipX()){
            region.flip(true, false);
            runningRight = false;
        } else if ((b2body.getLinearVelocity().x>0 || runningRight) && region.isFlipX()) {
            region.flip(true, false);
            runningRight = true;
        }
        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
        return region;
    }

    public State getState(){
        if(b2body.getLinearVelocity().y > 0 || (b2body.getLinearVelocity().y<0 && previousState == State.JUMPING)){
            return State.JUMPING;
        } else if (b2body.getLinearVelocity().y < 0){
            return State.FALLING;
        } else if (b2body.getLinearVelocity().x != 0){
            return State.RUNNING;
        } else {
            return State.STANDING;
        }
    }

    public void defineMario(){
        BodyDef bdef = new BodyDef();
        bdef.position.set(32 / MyGdxGame.PPM, 32 / MyGdxGame.PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();

        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MyGdxGame.PPM);
        fdef.filter.categoryBits = MyGdxGame.MARIO_BIT;
        fdef.filter.maskBits = MyGdxGame.GROUND_BIT |
                MyGdxGame.COIN_BIT |
                MyGdxGame.BRICK_BIT |
                MyGdxGame.ENEMY_BIT |
                MyGdxGame.OBJECT_BIT |
                MyGdxGame.ENEMY_HEAD_BIT
        ;

        fdef.shape = shape;
        b2body.createFixture(fdef);

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2/MyGdxGame.PPM, 6/MyGdxGame.PPM), new Vector2(2/MyGdxGame.PPM, 6/MyGdxGame.PPM));
        fdef.shape = head;
        fdef.isSensor = true;

        b2body.createFixture(fdef).setUserData("head");
    }
}