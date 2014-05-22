/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.handmark.pulltorefresh.library;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import com.wassabi.psmobile.R;

public class PullToRefreshRelativeLayout extends PullToRefreshBase<RelativeLayout> {

	public PullToRefreshRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected RelativeLayout createRefreshableView(Context context, AttributeSet attrs) {
		RelativeLayout RelativeLayout;
		if (VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
			RelativeLayout = new InternalRelativeLayoutSDK9(context, attrs);
		} else {
			RelativeLayout = new RelativeLayout(context, attrs);
		}

		RelativeLayout.setId(R.id.widgetView);
		return RelativeLayout;
	}

	@Override
	protected boolean isReadyForPullDown() {
		return mRefreshableView.getScrollY() == 0;
	}

	@Override
	protected boolean isReadyForPullUp() {
		View RelativeLayoutChild = mRefreshableView.getChildAt(0);
		if (null != RelativeLayoutChild) {
			return mRefreshableView.getScrollY() >= (RelativeLayoutChild.getHeight() - getHeight());
		}
		return false;
	}

	@TargetApi(9)
	final class InternalRelativeLayoutSDK9 extends RelativeLayout {

		public InternalRelativeLayoutSDK9(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX,
				int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

			final boolean returnValue = super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
					scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);

			// Does all of the hard work...
			OverscrollHelper
					.overScrollBy(PullToRefreshRelativeLayout.this, deltaY, scrollY, getScrollRange(), isTouchEvent);

			return returnValue;
		}

		/**
		 * Taken from the AOSP RelativeLayout source
		 */
		private int getScrollRange() {
			int scrollRange = 0;
			if (getChildCount() > 0) {
				View child = getChildAt(0);
				scrollRange = Math.max(0, child.getHeight() - (getHeight() - getPaddingBottom() - getPaddingTop()));
			}
			return scrollRange;
		}
	}
}
