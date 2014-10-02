package zhen.test.component;

import zhen.packet.RunTimeLayoutInformation;

import com.android.hierarchyviewerlib.models.ViewNode;
import com.android.hierarchyviewerlib.models.ViewNode.Property;
import com.android.hierarchyviewerlib.models.Window;

public class testLayoutInfo {

	public static void main(String[] args){
		new testLayoutInfo().checkAndCloseKeyboard();
	}
	
	public void checkAndCloseKeyboard(){
		System.out.println("start");
		RunTimeLayoutInformation layoutInfo= new RunTimeLayoutInformation("adb");
		layoutInfo.init();
		System.out.println("start 1 ");
		try { Thread.sleep(1000);
		} catch (InterruptedException e) { }
		Window selected = null;
		
		for(Window win:	layoutInfo.getWindowList()){
			System.out.println("title:"+win.getTitle());
			if(win.getTitle().contains("InputMethod")){
				selected = win;  
			}
		}
		
		
		System.out.println("start 2 ");
		System.out.println("selected:"+selected.getTitle());
		if(selected != null){
			ViewNode rootNode = layoutInfo.loadFocusedWindowData();
			while(rootNode.parent!=null){
				rootNode = rootNode.parent;
			}
			
			for(Property p  :rootNode.properties){
				System.out.println(p.name+"\t:\t"+p.value);
			}
			Property p = rootNode.namedProperties.get("layout:getWidth()");
			System.out.println(p.value);
			p = rootNode.namedProperties.get("layout:getHeight()");
			System.out.println(p.value);
		}
		
		layoutInfo.terminate();
		System.exit(0);
	}
}

/**
accessibility:getContentDescription()	:	null
accessibility:getImportantForAccessibility()	:	yes
accessibility:getLabelFor()	:	-1
bg_state_mUseColor	:	0
drawing:getAlpha()	:	1.0
drawing:getPersistentDrawingCache()	:	SCROLLING
drawing:getPivotX()	:	0.0
drawing:getPivotY()	:	0.0
drawing:getRotation()	:	0.0
drawing:getRotationX()	:	0.0
drawing:getRotationY()	:	0.0
drawing:getScaleX()	:	1.0
drawing:getScaleY()	:	1.0
drawing:getSolidColor()	:	0
drawing:getTranslationX()	:	0.0
drawing:getTranslationY()	:	0.0
drawing:getX()	:	0.0
drawing:getY()	:	0.0
drawing:isAlwaysDrawnWithCacheEnabled()	:	true
drawing:isChildrenDrawingOrderEnabled()	:	false
drawing:isChildrenDrawnWithCacheEnabled()	:	false
drawing:isDrawingCacheEnabled()	:	false
drawing:isOpaque()	:	false
drawing:mForeground	:	null
drawing:mForegroundGravity	:	119
drawing:mForegroundInPadding	:	true
drawing:mLayerType	:	NONE
drawing:willNotCacheDrawing()	:	false
drawing:willNotDraw()	:	true
events:mLastTouchDownIndex	:	-1
events:mLastTouchDownTime	:	0
events:mLastTouchDownX	:	0.0
events:mLastTouchDownY	:	0.0
focus:getDescendantFocusability()	:	FOCUS_AFTER_DESCENDANTS
focus:hasFocus()	:	false
focus:isFocusable()	:	false
focus:isFocused()	:	false
getFilterTouchesWhenObscured()	:	false
getScrollBarStyle()	:	INSIDE_OVERLAY
getTag()	:	null
getVisibility()	:	VISIBLE
isActivated()	:	false
isAnimationCacheEnabled()	:	true
isClickable()	:	false
isEnabled()	:	true
isFocusableInTouchMode()	:	false
isHapticFeedbackEnabled()	:	true
isHovered()	:	false
isInTouchMode()	:	true
isSelected()	:	false
isSoundEffectsEnabled()	:	true
layout:getBaseline()	:	-1
layout:getHeight()	:	1328
layout:getLayoutDirection()	:	RESOLVED_DIRECTION_LTR
layout:getRawLayoutDirection()	:	LTR
layout:getWidth()	:	1200
layout:hasTransientState()	:	false
layout:isLayoutRtl()	:	false
layout:layout_height	:	WRAP_CONTENT
layout:layout_width	:	MATCH_PARENT
layout:mBottom	:	1328
layout:mChildCountWithTransientState	:	0
layout:mLeft	:	0
layout:mRight	:	1200
layout:mTop	:	0
layout_flags	:	25166088
layout_flags_FLAG_HARDWARE_ACCELERATED	:	0x1000000
layout_flags_FLAG_LAYOUT_IN_SCREEN	:	0x100
layout_flags_FLAG_NOT_FOCUSABLE	:	0x8
layout_flags_FLAG_SPLIT_TOUCH	:	0x800000
layout_horizontalWeight	:	0.0
layout_type	:	TYPE_INPUT_METHOD
layout_verticalWeight	:	0.0
layout_x	:	0
layout_y	:	0
mGroupFlags	:	2375763
mGroupFlags_CLIP_CHILDREN	:	0x1
mGroupFlags_CLIP_TO_PADDING	:	0x2
mID	:	NO_ID
mPrivateFlags	:	16813368
mPrivateFlags_DRAWN	:	0x20
mSystemUiVisibility	:	0
mSystemUiVisibility_SYSTEM_UI_FLAG_VISIBLE	:	0x0
mViewFlags	:	402655360
measurement:mMeasureAllChildren	:	false
measurement:mMeasuredHeight	:	1328
measurement:mMeasuredWidth	:	1200
measurement:mMinHeight	:	0
measurement:mMinWidth	:	0
padding:mForegroundPaddingBottom	:	0
padding:mForegroundPaddingLeft	:	0
padding:mForegroundPaddingRight	:	0
padding:mForegroundPaddingTop	:	0
padding:mPaddingBottom	:	0
padding:mPaddingLeft	:	0
padding:mPaddingRight	:	0
padding:mPaddingTop	:	0
padding:mUserPaddingBottom	:	0
padding:mUserPaddingEnd	:	-2147483648
padding:mUserPaddingLeft	:	0
padding:mUserPaddingRight	:	0
padding:mUserPaddingStart	:	-2147483648
scrolling:mScrollX	:	0
scrolling:mScrollY	:	0
text:getRawTextAlignment()	:	GRAVITY
text:getRawTextDirection()	:	INHERIT
text:getTextAlignment()	:	GRAVITY
text:getTextDirection()	:	FIRST_STRONG
title:com.android.quicksearchbox/com.android.quicksearchbox.SearchActivity
title:com.android.systemui/com.android.systemui.recent.RecentsActivity
title:com.android.launcher/com.android.launcher2.Launcher
title:com.example.testpopup/com.example.testpopup.AndroidPopupWindowActivity
title:com.android.systemui.ImageWallpaper




**/