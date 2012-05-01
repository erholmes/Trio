package com.barcicki.trio.core;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.barcicki.trio.R;

public class CardGrid extends RelativeLayout {

	private static int ID_OFFSET = 3123;
	private static int DEFAULT_NO_OF_COLUMNS = 4;
	private static int MAX_DEFAULT_NO_OF_COLUMNS = 5;

	private ArrayList<CardView> mCardViews = new ArrayList<CardView>();
	private OnClickListener mOnClickListener = null;
	private CardList mCards = new CardList();

	private boolean mForceColumns = false;
	private int mForcedColumns = 0;
	private boolean mRenderedViews = false;
	private int mWidth = 0;
	private int mHeight = 0;
	private AnimationListener mRevealCardAnimationListener;

	public CardGrid(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public CardGrid(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public CardGrid(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void setOnItemClickListener(OnClickListener listener) {
		mOnClickListener = listener;
		updateListener();
	}

	public void setCards(CardList cards) {
		mCards.clear();
		mCardViews.clear();
		removeAllViews();

		if (cards != null) {
			mCards.addAll(cards);
		}

		int id = 1;
		for (Card card : cards) {

			CardView cardView = new CardView(getContext());
			cardView.setId(ID_OFFSET + id++);
			cardView.setCard(card);
			cardView.setImageResource(R.drawable.square_reverse);
			cardView.setOnClickListener(mOnClickListener);

			addView(cardView);
			mCardViews.add(cardView);
		}

		// requestLayout();
		// invalidate();

		// if (getMeasuredWidth() > 0 && getMeasuredHeight() > 0) {
		// render();
		// } else {
		// requestLayout();
		// invalidate();
		// refreshDrawableState();
		// }

		// invalidate();
		// refreshDrawableState();

	}

	public void setCards(ArrayList<CardList> cards) {
		// mCards.clear();
		// mCardViews.clear();

		CardList sum = new CardList();
		for (CardList list : cards) {
			sum.addAll(list);
		}

		setCards(sum);
	}

	public CardList getCards() {
		if (mCards == null)
			mCards = new CardList();
		return mCards;
	}

	public CardList getCardsFromViews() {
		CardList set = new CardList();
		for (CardView cv : mCardViews) {
			set.add(cv.getCard());
		}
		return set;
	}

	public void forceColumnSize(Integer value) {
		if (value == null) {
			mForceColumns = false;
		} else {
			mForceColumns = true;
			mForcedColumns = value;
		}
	}

	public void render() {
		mRenderedViews = false;
		renderCards(getMeasuredWidth(), getMeasuredHeight());
	}

	public int getColmuns() {
		if (mForceColumns) {
			return mForcedColumns;
		} else {
			return getDesiredColumnSize(getCards().size());
		}
	}

	public void updateGrid(final CardList updatedCards) {
		CardList updated = new CardList(updatedCards);
		ArrayList<CardView> replaceable = new ArrayList<CardView>();
		for (CardView cv : mCardViews) {
			Card c = cv.getCard();
			if (!updated.contains(c)) {
				replaceable.add(cv);
			} else {
				updated.remove(c);
			}
		}

		if (Trio.LOCAL_LOGV) {
			Log.v("Classic Game",
					"Updated " + updated.size() + " " + updated.toString()
							+ "; Replaceable " + replaceable.size());
		}

		if (replaceable.size() == updated.size()) {
			for (int i = 0; i < replaceable.size(); i++) {
				CardView cv = replaceable.get(i);
				cv.animateSwitchCard(updated.get(i));
			}
		} else {
			final Animation hide = AnimationUtils.loadAnimation(getContext(),
					R.anim.grid_hide);
			final Animation show = AnimationUtils.loadAnimation(getContext(),
					R.anim.grid_show);
			hide.setAnimationListener(new AnimationListener() {

				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub

				}

				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					setCards(updatedCards);
					render();
					startAnimation(show);
				}

			});
			startAnimation(hide);

		}
	}

	public void showReverse() {
		if (Trio.LOCAL_LOGD)
			Log.d("CardGrid", "Hid " + mCardViews.size() + " cards");
		for (CardView cv : mCardViews) {
			cv.setOverdraw(false);
			cv.invalidate();
			cv.refreshDrawableState();
			cv.setOnClickListener(null);
		}
	}

	public void hideReverse() {
		for (CardView cv : mCardViews) {
			cv.setOverdraw(true);
			cv.invalidate();
			cv.refreshDrawableState();
			cv.setOnClickListener(mOnClickListener);
		}
	}

	public CardView select(Card card) {
		for (CardView cv : mCardViews) {
			if (cv.getCard().equals(card)) {
				cv.setSelected(true);
				cv.invalidate();
				cv.refreshDrawableState();
				return cv;
			}
		}
		return null;
	}

	public void deselectAll() {
		for (CardView cv : mCardViews) {
			cv.setSelected(false);
			cv.invalidate();
			cv.refreshDrawableState();
		}
	}

	public void setResourceImageForAll(int resId) {
		for (CardView cv : mCardViews) {
			cv.setImageResource(resId);
			cv.invalidate();
			cv.refreshDrawableState();
		}
	}

	public void setRevealCardListener(AnimationListener animationListener) {
		mRevealCardAnimationListener = animationListener;
	}

	public void revealCard(CardList cardList) {
		int size = cardList.size();

		int position = 0;
		while (position + size <= mCardViews.size()) {
			
			if (Trio.LOCAL_LOGD) 
				Log.d("CardGrid", "Try to reveal cards at " + position);
			
			List<CardView> cardViews = mCardViews.subList(position, position
					+ size);
			CardList row = new CardList();
			for (CardView cv : cardViews) {
				row.add(cv.getCard());
			}

			if (CardList.areEqual(row, cardList)) {
				
				if (Trio.LOCAL_LOGD) 
					Log.d("CardGrid", "Found and revealing  " + row);
				
				cardViews.get(0).setRevealAnimationListener(
						mRevealCardAnimationListener);
				for (CardView cv : cardViews) {
					cv.animateReveal();
				}
				
				return;
			}

			position += size;
		}

	}

	public static int getDesiredColumnSize(int size) {
		if (size > Trio.DEFUALT_TABLE_SIZE) {
			return MAX_DEFAULT_NO_OF_COLUMNS;
		} else {
			return DEFAULT_NO_OF_COLUMNS;
		}
	}

	private void updateListener() {
		for (CardView cv : mCardViews) {
			cv.setOnClickListener(mOnClickListener);
		}
	}

	private int renderCards(int width, int height) {
		if (Trio.LOCAL_LOGD)
			Log.d("CardGrid", "Render called: " + width + "x" + height);

		if (!mRenderedViews || mWidth != width || mHeight != height) {
			

			int cards_size = getCards().size();

			if (cards_size > 0) {

				CardGridSize gridSize = CardGridSize.getGridSize(cards_size);

				int column_size = gridSize.getRowSize(0);
				int rows = gridSize.getRowsSize();

				int lower_size = (width > height) ? height : width;
				int lower_divider = (column_size > rows) ? rows : column_size;

				int card_container = lower_size / lower_divider;
				int padding = (int) (5f / 100 * card_container);
				int card_size = card_container - 2 * padding;

				int available_width = width - card_container * column_size;
				int available_height = height - card_container * rows;

				int padding_horizontal = padding;
				int padding_vertical = padding;

				if (available_width > 0) {
					padding_horizontal += available_width / (2 * column_size);
				}

				if (available_height > 0) {
					padding_vertical += available_height / (2 * rows);
				}

				if (Trio.LOCAL_LOGD)
					Log.d("CardGrid", "Size: " + card_size
							+ " Padding Top/Bottom: " + padding_vertical
							+ " Padding Left/Right: " + padding_horizontal);

				int position = 0;
				for (CardView cv : mCardViews) {
					int row = gridSize.getRow(position);
					int column = gridSize.getColumn(position);

					if (Trio.LOCAL_LOGD)
						Log.d("CardGrid", "Row: " + row + " Columnd: " + column);
					position++;
					//
					int real_padding_horizontal = padding_horizontal;
					if (!gridSize.isRowFull(row)) {
						int row_size = gridSize.getRowSize(row);
						real_padding_horizontal = padding
								+ ((width - card_container * row_size) / (2 * row_size));
					}

					LayoutParams params = new LayoutParams(card_size, card_size);
//					LayoutParams params = (RelativeLayout.LayoutParams) cv
//							.getLayoutParams();

//					params.width = card_size;
//					params.height = card_size;
					params.addRule(ALIGN_PARENT_LEFT, TRUE);
					params.addRule(ALIGN_PARENT_TOP, TRUE);
					params.setMargins(
							real_padding_horizontal
									+ (column * (2 * real_padding_horizontal + card_size)),
							padding_vertical
									+ (row * (2 * padding_vertical + card_size)),
							real_padding_horizontal, padding_vertical);
					
//					params.width = card_size;
//					params.height = card_size;
					cv.setLayoutParams(params);
//					cv.forceMeasureSize(cards_size, cards_size);
				}

				mHeight = height;
				mWidth = width;
				mRenderedViews = true;
				return card_size;
			}

		}
		return -1;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		if (Trio.LOCAL_LOGD)
			Log.d("CardGrid", "onMeasure " + widthSize + "x" + heightSize);		

		// getLayoutParams().width = widthSize;
		// getLayoutParams().height = heightSize;

		setMeasuredDimension(widthSize, heightSize);
		renderCards(widthSize, heightSize);
		
//		forceLayout();
//		int size = 
//		int measure = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
		
//		for (CardView cv : mCardViews) {
//			cv.measure(measure, measure);
//		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	// @Override
	// protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	//
	// if (Trio.LOCAL_LOGD)
	// Log.d("CardGrid", "W: " + widthMeasureSpec + " H: "
	// + heightMeasureSpec);
	//
	// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	// }

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
//		renderCards(r - l, b - t);
		super.onLayout(changed, l, t, r, b);
	}

	// @Override
	// public void draw(Canvas canvas) {
	// if (!mRenderedViews) {
	// renderCards(canvas.getWidth(), canvas.getHeight());
	// }
	// super.draw(canvas);
	// }

	// @Override
	// protected void onFinishInflate() {
	// if (Trio.LOCAL_LOGD)
	// Log.d("CardGrid", "onFinishInflate");
	//
	// if (!mCards.isEmpty()) {
	// mCardViews.clear();
	// for (Card card : mCards) {
	// CardView cv = new CardView(getContext());
	// mCardViews.add(cv);
	// addView(cv);
	// }
	// invalidate();
	// }
	//
	// super.onFinishInflate();
	// }

//	@Override
//	protected void onLayout(boolean changed, int l, int t, int r, int b) {
//		// if (!mRenderedViews) {
//		// renderCards(r - l, b - t);
//		// changed = true;
//		// // invalidate();
//		// // refreshDrawableState();
//		// // }
//		// // super.onLayout(changed, l, t, r, b);
//		// invalidate();
//		super.onLayout(changed, l, t, r, b);
//	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (Trio.LOCAL_LOGD)
			Log.d("CardGrid",
					"onDraw: " + canvas.getWidth() + "x" + canvas.getHeight());

		canvas.drawText("asdasd", 0.5f, 0.5f, new Paint());

		super.onDraw(canvas);
	}

	// @Override
	// public void invalidate() {
	// mRenderedViews = false;
	// requestLayout();
	// super.invalidate();
	// }
}
