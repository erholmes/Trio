package com.barcicki.trio;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.barcicki.trio.core.Card;
import com.barcicki.trio.core.CardGrid;
import com.barcicki.trio.core.CardList;
import com.barcicki.trio.core.CardView;
import com.barcicki.trio.core.Trio;
import com.barcicki.trio.core.TrioGameActivity;
import com.barcicki.trio.core.TrioSettings;

public class ClassicGameActivity extends TrioGameActivity {
	private static int NUMBER_OF_HINTS = 10;

	SharedPreferences mPrefs;
	Trio mTrio = new Trio();

	Button mHintButton;

	TextView mDeckStatus;
	CardList gSelectedCards = new CardList();
	ArrayList<CardView> gSelectedViews = new ArrayList<CardView>();

	int gTriosFound = 0;
	int gHintsRemained = NUMBER_OF_HINTS;
	boolean gRestoredGame = false;

	CardGrid mCardGrid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.classic);
		super.onCreate(savedInstanceState);

		setHelpFragments(
				R.layout.help_classic_fragment1,
				R.layout.help_classic_fragment2,
				R.layout.help_classic_fragment3);

		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mCardGrid = (CardGrid) findViewById(R.id.cardsContainer);
		mHintButton = (Button) findViewById(R.id.gameHintButton);
		mDeckStatus = (TextView) findViewById(R.id.gameDeckStatus);
		
		if (restoreSavedGame()) {
			gRestoredGame = true;
		} else {
			mTrio.newGame();
		}

		attachCardListeners();
		startGame();

		hideOverlays();
		
		if (!TrioSettings.hasSeenClassicHelp()) {
			showHelpOverlay();
			TrioSettings.setSeenClassicHelp(true);
		}

	}

	private void startGame() {
		mCardGrid.setCards(mTrio.getTable());
		mCardGrid.hideReverse();
		mHintButton.setText(getString(R.string.classic_hint, gHintsRemained));
		mDeckStatus.setText(((Integer) mTrio.getGame().size()).toString());
		startTimer();
	}

	private void resumeGame() {
		mCardGrid.hideReverse();
		startTimer();
	}

	private void pauseGame() {
		mCardGrid.showReverse();
		pauseTimer();
	}
	
	@Override
	public void onGameFinished() {
		showEndingPause();
	}
	
	@Override
	public void onGameReset() {
		gRestoredGame = false;
		setElapsedTime(0L);
		gTriosFound = 0;
		gHintsRemained = NUMBER_OF_HINTS;
		gSelectedCards = new CardList();
		gSelectedViews = new ArrayList<CardView>();
		mHintButton.setText(getString(R.string.classic_hint, gHintsRemained));
		mDeckStatus.setText(((Integer) mTrio.getGame().size()).toString());
	}

//	private void resetGameStatus() {
//		gRestoredGame = false;
//		gGameEnded = false;
//		gElapsedTime = 0L;
//		gTriosFound = 0;
//		gHintsRemained = NUMBER_OF_HINTS;
//		gSelectedCards = new CardList();
//		gSelectedViews = new ArrayList<CardView>();
//		mHintButton.setText(getString(R.string.classic_hint, gHintsRemained));
//		mDeckStatus.setText(((Integer) mTrio.getGame().size()).toString());
//	}

	@Override
	protected void onShowPauseOverlay() {
		pauseGame();

		TextView timeView = (TextView) getPauseOverlay().findViewById(
				R.id.gameTime);
		TextView hintsView = (TextView) getPauseOverlay().findViewById(
				R.id.gameHintCount);
		TextView statusView = (TextView) getPauseOverlay().findViewById(
				R.id.gameStatus);
		TextView trioView = (TextView) getPauseOverlay().findViewById(
				R.id.gameTrioCount);

		timeView.setText(getElapsedTimeAsString());
		hintsView
				.setText(getString(R.string.classic_hint_count, gHintsRemained));
		trioView.setText(getString(R.string.classic_trio_count, gTriosFound));
		statusView.setText(getString(R.string.classic_status, mTrio.getGame()
				.size()));

		Button buttonContinue = (Button) findViewById(R.id.gameContinue);
		buttonContinue.setText(getString(R.string.pause_continue));

		Button buttonNewGame = (Button) findViewById(R.id.gameNew);
		buttonNewGame.setVisibility(View.VISIBLE);

		Button buttonQuit = (Button) findViewById(R.id.gameQuit);
		buttonQuit.setText(getString(R.string.pause_save_quit));
	}

	@Override
	protected void onHidePauseOverlay() {
		resumeGame();
	}

	@Override
	protected void onShowHelpOverlay() {
		pauseGame();
	}

	@Override
	protected void onHideHelpOverlay() {
		resumeGame();
	}


	private void attachCardListeners() {

		mCardGrid.setOnItemClickListener(new OnClickListener() {
			public void onClick(View v) {
				CardView cv = (CardView) v;
				Card card = cv.getCard();

				if (!gSelectedCards.contains(card)) {

					gSelectedCards.add(card);
					gSelectedViews.add(cv);

					cv.setSelected(true);

					if (Trio.LOCAL_LOGV)
						Log.v("Classic Game", "Selected " + card.toString());
				} else {

					gSelectedCards.remove(card);
					gSelectedViews.remove(cv);

					cv.setSelected(false);

					if (Trio.LOCAL_LOGV)
						Log.v("Classic Game", "Deselected " + card.toString());
				}

				if (gSelectedCards.size() == 3) {

					if (gSelectedCards.hasTrio()) {
						onTrioFound(gSelectedCards, gSelectedViews);
					} else {
						onNotTrioFound(gSelectedCards, gSelectedViews);
					}

					gSelectedCards.clear();
					gSelectedViews.clear();

				} else {
					makeClickSound();
				}
			}
		});
	}

	@Override
	protected void onPause() {
		if (!isGameFinished()) {
			saveGame();
		}
		showPauseOverlay();
		super.onPause();
	}

	protected void onNotTrioFound(CardList selectedCards,
			ArrayList<CardView> selectedViews) {
		if (Trio.LOCAL_LOGV)
			Log.v("Classic Game", "Trio NOT found");

		makeFailSound();

		for (CardView cv : selectedViews) {
			cv.animateFail();
		}

		if (TrioSettings.displaysWhatIsWrong()) {
			displayWhatIsWrong(selectedCards);
		}

	}

	protected void onTrioFound(CardList selectedCards,
			ArrayList<CardView> selectedViews) {
		if (Trio.LOCAL_LOGV)
			Log.v("Classic Game", "Trio found");

		makeSuccessSound();

		mTrio.foundTrio(selectedCards);
		mCardGrid.updateGrid(mTrio.getTable());
		mDeckStatus.setText(((Integer) mTrio.getGame().size()).toString());

		gTriosFound += 1;

		if (!mTrio.getTable().hasTrio() && !mTrio.getGame().hasNext()) {
			finishGame();
		} else {

		}

	}

	private boolean saveGame() {

		SharedPreferences.Editor ed = mPrefs.edit();

		ed.putString("game_string", mTrio.getGameString());
		ed.putString("game", mTrio.getGame().toString());
		ed.putString("table", mTrio.getTable().toString());
		ed.putInt("trios_found", gTriosFound);
		ed.putInt("hints", gHintsRemained);
		ed.putLong("time", getElapsedTime());
		ed.putBoolean("saved_game", true);

		if (ed.commit()) {
			if (Trio.LOCAL_LOGV)
				Log.v("Classic Game", "saved progress");
			return true;
		} else {
			Log.e("Classic Game", "failed to save progress");
			return false;
		}
	}

	private boolean clearSavedGame() {

		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putBoolean("saved_game", false);

		if (ed.commit()) {
			if (Trio.LOCAL_LOGV)
				Log.v("Classic Game", "unsaved progress");
			return true;
		} else {
			Log.e("Classic Game", "failed to change progress");
			return false;
		}

	}

	private boolean restoreSavedGame() {
		
		if (mPrefs.getBoolean("saved_game", false)) {

			String game = mPrefs.getString("game", "");
			String table = mPrefs.getString("table", "");
			String gameString = mPrefs.getString("game_string", "");
			gHintsRemained = mPrefs.getInt("hints", NUMBER_OF_HINTS);
			gTriosFound = mPrefs.getInt("trios_found", 0);
			setElapsedTime(mPrefs.getLong("time", 0L));

			onTimerTick();

			if (!game.equals("") && !table.equals("")) {

				mTrio.setGame(CardList.fromString(mTrio.getDeck(), game));
				mTrio.setTable(CardList.fromString(mTrio.getDeck(), table));
				mTrio.setGameString(gameString);

				if (Trio.LOCAL_LOGV)
					Log.v("Classic Game", "Restored saved game");
				return true;

			} else {
				Log.e("TrioState", "failed to restore saved game");
			}

		}

		return false;
	}

	/**
	 * Button handlers
	 */

	public void onHintClicked(View v) {
		makeClickSound();
		if (gHintsRemained > 0) {
			ArrayList<CardList> trios = mTrio.getTable().getTrios();
			int selectedSize = gSelectedCards.size();

			if (trios.size() > 0) {

				// if no cards were selected before, select the first card
				// possible - it is being done in the end of method

				// if one card was selected, check if it makes trio with
				// others. If yes, show next card, if no, deselect it and
				// select another one
				if (selectedSize == 1) {

					Card selected = gSelectedCards.get(0);

					for (CardList trio : trios) {

						if (trio.contains(selected)) {
							for (Card c : trio) {
								if (!c.equals(selected)) {

									gSelectedCards.add(c);
									gSelectedViews.add(mCardGrid.select(c));

									useHint();

									if (Trio.LOCAL_LOGV)
										Log.v("Single Game Hint",
												"Hint showed second card");
									return;
								}
							}
						}

					}

					// if there're two cards selected check if they make any
					// trio, if yes do not give additional hints, if no
					// deselect them and select first one
				} else if (selectedSize == 2) {
					Card sel1 = gSelectedCards.get(0);
					Card sel2 = gSelectedCards.get(1);
					for (CardList trio : trios) {
						if (trio.contains(sel1) && trio.contains(sel2)) {
							Toast.makeText(getApplicationContext(),
									getString(R.string.hints_ended),
									Toast.LENGTH_SHORT).show();
							return;
						}
					}
				}

				gSelectedCards.clear();
				Card firstCard = trios.get(0).get(0);

				mCardGrid.deselectAll();
				gSelectedCards.clear();
				gSelectedViews.clear();
				gSelectedCards.add(firstCard);
				gSelectedViews.add(mCardGrid.select(firstCard));

				useHint();

				if (Trio.LOCAL_LOGV)
					Log.v("Single Game Hint", "Hint showed first card");
				return;
			} else {
				Log.e("Single Game Hint",
						"Trios were empty when they shouldn't");
			}
		} else {
			Toast.makeText(this, getString(R.string.classic_hints_finished),
					Toast.LENGTH_SHORT).show();
		}
	}

	public void useHint() {
		gHintsRemained -= 1;
		setElapsedTime(getElapsedTime() + 30000); 
		startTimer();
		mHintButton.setText(getString(R.string.classic_hint, gHintsRemained));
	}

	public void showEndingPause() {
		showPauseOverlay();

		TextView statusView = (TextView) getPauseOverlay().findViewById(
				R.id.gameStatus);
		statusView.setText(getString(R.string.classic_end));

		Button buttonContinue = (Button) getPauseOverlay().findViewById(
				R.id.gameContinue);
		buttonContinue.setVisibility(View.GONE);

		Button buttonNewGame = (Button) getPauseOverlay().findViewById(
				R.id.gameNew);
		buttonNewGame.setVisibility(View.VISIBLE);

		Button buttonQuit = (Button) getPauseOverlay().findViewById(
				R.id.gameQuit);
		buttonQuit.setText(getString(R.string.pause_quit));
	}

	public void onPressedContinue(View v) {
		makeClickSound();
		hidePauseOverlay();
	}

	public void onPressedNewGame(View v) {
		makeClickSound();
		mTrio.newGame();

		// if (gGameEnded) {
		mCardGrid.setCards(mTrio.getTable());
		// mCardGrid.render();
		// } else {
		// mCardGrid.updateGrid(mTrio.getTable());
		// }

		resetGame();
		// gGameStarted = true;
		// hidePause();

		hidePauseOverlay();
	}

	// public void onPressedRestartGame(View v) {
	// makeClickSound();
	// mTrio.restartGame(mTrio.getGameString());
	//
	// // if (gGameEnded) {
	// mCardGrid.setCards(mTrio.getTable());
	// // mCardGrid.render();
	// // } else {
	// // mCardGrid.updateGrid(mTrio.getTable());
	// // }
	//
	// resetGameStatus();
	// showStartPause();
	// }

	public void onPressedQuitGame(View v) {
		makeClickSound();

		if (isGameFinished()) {
			clearSavedGame();
		} else {
			saveGame();
		}

		startHomeActivity();
		finish();
	}

}
