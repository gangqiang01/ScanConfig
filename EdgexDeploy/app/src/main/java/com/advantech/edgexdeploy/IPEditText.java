package com.advantech.edgexdeploy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class IPEditText extends LinearLayout {
    private static final String TAG = "IPEditText";
    private EditText mFirstIP;
    private EditText mSecondIP;
    private EditText mThirdIP;
    private EditText mFourthIP;
    private boolean NullAndPoint = false;

    public IPEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        View view = LayoutInflater.from(context).inflate(
                R.layout.ipedittext, this);
        mFirstIP = findViewById(R.id.ip_first);
        mSecondIP = findViewById(R.id.ip_second);
        mThirdIP = findViewById(R.id.ip_third);
        mFourthIP = findViewById(R.id.ip_fourth);
        OperatingEditText(context);
    }

    /**
     * 获得EditText中的内容,当每个Edittext的字符达到三位时,自动跳转到下一个EditText,当用户点击.时,
     * 下一个EditText获得焦点
     */
    private void OperatingEditText(final Context context) {
        mFirstIP.addTextChangedListener(new TextWatcher() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                /*
                 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
                 * 用户点击.时,下一个EditText获得焦点
                 */
                if (s != null && s.length() > 0 && mFirstIP.getSelectionStart() != 0) {
                    Log.i(TAG, s.toString());
                    if (!s.toString().contains(".") && Integer.parseInt(s.toString()) > 255) {
                        Toast.makeText(context, "请输入合法的格式",
                                Toast.LENGTH_LONG).show();
                        mFirstIP.setText("255");
                        mFirstIP.setSelection(0);
                    } else if (s.length() > 2) {
                        mSecondIP.setFocusable(true);
                        mSecondIP.requestFocus();
                        if (mSecondIP.getText().toString().length() > 0) {
                            mSecondIP.selectAll();
                        }
                    } else {
                        mFirstIP.requestFocus();
                        mFirstIP.setSelection(s.length());
                    }
                    if (s.toString().length() == 1 && s.toString().equals(".")) {
                        mFirstIP.setText("");
                    }
                    if (s.toString().contains(".") && s.toString().length() > 1) {
//mFirstIP.setText(s.subSequence(0,s.length()-1));
                        mFirstIP.setText(s.toString().subSequence(0, s.toString().indexOf(".")) + s.toString().substring(s.toString().indexOf(".") + 1, s.toString().length()));
                        mSecondIP.setFocusable(true);
                        mSecondIP.requestFocus();
                        if (mSecondIP.getText().toString().length() > 0)
                            mSecondIP.selectAll();
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mSecondIP.addTextChangedListener(new TextWatcher() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                /*
                 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
                 * 用户点击啊.时,下一个EditText获得焦点
                 */
                if (s != null && s.length() > 0 && mSecondIP.getSelectionStart() != 0) {
                    Log.i(TAG, s.toString());
                    if (!s.toString().contains(".") && Integer.parseInt(s.toString()) > 255) {
                        Toast.makeText(context, "请输入合法的格式",
                                Toast.LENGTH_LONG).show();
                        mSecondIP.setText("255");
                        mSecondIP.setSelection(0);
                    } else if (s.length() > 2) {
                        mThirdIP.setFocusable(true);
                        mThirdIP.requestFocus();
                        if (mThirdIP.getText().toString().length() > 0) {
                            mThirdIP.selectAll();
                        }
                    } else {
                        mSecondIP.requestFocus();
                        mSecondIP.setSelection(s.length());
                    }
                    if (s.toString().length() == 1 && s.toString().equals(".")) {
                        NullAndPoint = true;
                        mSecondIP.setText("");
                    }
                    if (s.toString().contains(".") && s.toString().length() > 1) {
//mFirstIP.setText(s.subSequence(0,s.length()-1));
                        mSecondIP.setText(s.toString().subSequence(0, s.toString().indexOf(".")) + s.toString().substring(s.toString().indexOf(".") + 1, s.toString().length()));
                        mThirdIP.setFocusable(true);
                        mThirdIP.requestFocus();
                        if (mThirdIP.getText().toString().length() > 0)
                            mThirdIP.selectAll();
                    }
                }
                /*
                 * 当用户需要删除时,此时的EditText为空时,上一个EditText获得焦点
                 */
                    if (s == null || start == 0 && s.length() == 0) {
                        if (NullAndPoint) {
                            NullAndPoint = false;
                        } else {
                            mFirstIP.setFocusable(true);
                            mFirstIP.requestFocus();
                            mFirstIP.setSelection(mFirstIP.getText().length());
                        }
                    }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mThirdIP.addTextChangedListener(new TextWatcher() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                /*
                 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
                 * 用户点击啊.时,下一个EditText获得焦点
                 */
                if (s != null && s.length() > 0 && mThirdIP.getSelectionStart() != 0) {
                    Log.i(TAG, s.toString());
                    if (!s.toString().contains(".") && Integer.parseInt(s.toString()) > 255) {
                        Toast.makeText(context, "请输入合法的格式",
                                Toast.LENGTH_LONG).show();
                        mThirdIP.setText("255");
                        mThirdIP.setSelection(0);
                    } else if (s.length() > 2) {
                        mFourthIP.setFocusable(true);
                        mFourthIP.requestFocus();
                        if (mFourthIP.getText().toString().length() > 0) {
                            mFourthIP.selectAll();
                        }
                    } else {
                        mThirdIP.requestFocus();
                        mThirdIP.setSelection(s.length());
                    }
                    if (s.toString().length() == 1 && s.toString().equals(".")) {
                        NullAndPoint = true;
                        mThirdIP.setText("");
                    }
                    if (s.toString().contains(".") && s.toString().length() > 1) {
//mFirstIP.setText(s.subSequence(0,s.length()-1));
                        mThirdIP.setText(s.toString().subSequence(0, s.toString().indexOf(".")) + s.toString().substring(s.toString().indexOf(".") + 1, s.toString().length()));
                        mFourthIP.setFocusable(true);
                        mFourthIP.requestFocus();
                        if (mFourthIP.getText().toString().length() > 0)
                            mFourthIP.selectAll();
                    }
                }
                /*
                 * 当用户需要删除时,此时的EditText为空时,上一个EditText获得焦点
                 */
                if (s == null || start == 0 && s.length() == 0) {
                    if (NullAndPoint) {
                        NullAndPoint = false;
                    } else {
                        mSecondIP.setFocusable(true);
                        mSecondIP.requestFocus();
                        mSecondIP.setSelection(mSecondIP.getText().length());
                    }
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mFourthIP.addTextChangedListener(new TextWatcher() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                /*
                 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
                 * 用户点击啊.时,下一个EditText获得焦点
                 */
                if (s != null && s.length() > 0 && mFourthIP.getSelectionStart() != 0) {
                    Log.i(TAG, s.toString());
                    if (!s.toString().contains(".") && Integer.parseInt(s.toString()) > 255) {
                        Toast.makeText(context, "请输入合法的格式",
                                Toast.LENGTH_LONG).show();
                        mFourthIP.setText("255");
                        mFourthIP.setSelection(0);
                    } else {
                        mFourthIP.requestFocus();
                        mFourthIP.setSelection(s.length());
                    }
                    if (s.toString().length() == 1 && s.toString().equals(".")) {
                        NullAndPoint = true;
                        mFourthIP.setText("");
                    }
                    if (s.toString().contains(".") && s.toString().length() > 1) {
//mFirstIP.setText(s.subSequence(0,s.length()-1));
                        mFourthIP.setText(s.toString().subSequence(0, s.toString().indexOf(".")) + s.toString().substring(s.toString().indexOf(".") + 1, s.toString().length()));
                    }
                }
                /*
                 * 当用户需要删除时,此时的EditText为空时,上一个EditText获得焦点
                 */
                if (s == null || start == 0 && s.length() == 0) {
                    if (NullAndPoint) {
                        NullAndPoint = false;
                    } else {
                        mThirdIP.setFocusable(true);
                        mThirdIP.requestFocus();
                        mThirdIP.setSelection(mThirdIP.getText().length());
                    }
                }


            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public String getText() {
        if (TextUtils.isEmpty(mFirstIP.getText().toString()) || TextUtils.isEmpty(mSecondIP.getText().toString())
                || TextUtils.isEmpty(mThirdIP.getText().toString()) || TextUtils.isEmpty(mFourthIP.getText().toString())) {
            return "";
        }
        String ipadress = mFirstIP.getText().toString() + "." + mSecondIP.getText().toString() + "." + mThirdIP.getText().toString() + "." + mFourthIP.getText().toString();
        Log.i(TAG, "控件返回结果值：" + ipadress);
        return ipadress;
    }

    public void setText(String s) {
        if (s == null) return;
        String[] ss = s.split("[.:]");
        for (int i = 0; i < ss.length; i++) {
            if (i == 0) mFirstIP.setText(ss[0]);
            else if (i == 1) mSecondIP.setText(ss[1]);
            else if (i == 2) mThirdIP.setText(ss[2]);
            else if (i == 3) mFourthIP.setText(ss[3]);
            Log.i(TAG, mFirstIP.getText().toString() + "　" + mSecondIP.getText().toString() + " " + mThirdIP.getText().toString() + " " + mFourthIP.getText().toString());
        }
    }
}
