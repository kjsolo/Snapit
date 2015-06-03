package cn.soloho.snapit.view;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class ParallaxedView {
	protected WeakReference<View> view;
	protected List<Animation> animations;

	public ParallaxedView(View view) {
		this.animations = new ArrayList<>();
		this.view = new WeakReference<>(view);
	}

	public boolean is(View v) {
		return (v != null && view != null && view.get() != null && view.get().equals(v));
	}

	public void setOffset(float offset) {
		View view = this.view.get();
		if (view != null) {
            view.setTranslationY(offset);
        }
	}

	public void setAlpha(float alpha) {
		View view = this.view.get();
		if (view != null) {
            view.setAlpha(alpha);
        }
	}
	
	protected synchronized void addAnimation(Animation animation) {
		animations.add(animation);
	}

	protected synchronized void animateNow() {
		View view = this.view.get();
		if (view != null) {
			AnimationSet set = new AnimationSet(true);
			for (Animation animation : animations)
				if (animation != null)
					set.addAnimation(animation);
			set.setDuration(0);
			set.setFillAfter(true);
			view.setAnimation(set);
			set.start();
			animations.clear();
		}
	}
	
	public void setView(View view) {
		this.view = new WeakReference<>(view);
	}
}
