package com.example.myapplication;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;
class Node {
    int sibling;
    int children;
    char ch;

    public Node(int sib, int chi, char c) {
        this.sibling = sib;
        this.children = chi;
        this.ch = c;
    }
}

class deleteNode {

    int current_off;
    int previous_off;
    char ch;

    public deleteNode(int curr, int pre, char c) {
        this.current_off = curr;
        this.previous_off = pre;
        this.ch = c;
    }

}

class The_Comparator implements Comparator<String> {
    public int compare(String str1, String str2) {
        String s1;
        String s2;
        s1 = str1;
        s2 = str2;
        // return second_Str.compareTo(first_Str);
        if (s1.length() < s2.length()) {
            return -1;
        }
        if (s1.length() > s2.length()) {
            return 1;
        }
        if (s1.compareTo(s2) < 0) {
            return -1;
        }
        if (s1.compareTo(s2) > 0) {
            return 1;
        } else
            return 0;
    }
}
public class MainActivity extends AppCompatActivity {
    private static final int UPDATE_UI = 1;


    EditText insertEditText, searchEditText, deleteEditText, prefixMatchEditText, prefixMatchWordEditText, queryCompleteEditText;
    Button initializeButton, insertButton, searchButton, deleteButton, prefixMatchButton, prefixMatchWordButton, queryCompleteButton, showAllButton, DictionaryButton;
    TextView insertTextView, searchTextView, deleteTextView, prefixMatchTextView, prefixMatchWordTextView, queryCompleteTextView, showAllTextView;
    public int end = 9;
    public static MappedByteBuffer trie, DeleteTrie, AvalStack;

    public int deleteTop = 0;
    public static int availableTop = 0;

    public Node getNode(int currOff) {
        return new Node(Math.abs(trie.getInt(currOff)), Math.abs(trie.getInt(currOff + 4)), trie.getChar(currOff + 8));
    }


    public int newNodeNumber() {

        if (availableTop >= 4) {
            int temp = availableTop;
            availableTop = availableTop - 4;

            return AvalStack.getInt(temp);
        } else {
            return end = end + 10;
        }
    }

    public void initializeTrie(String trieFileName, String deleteStackFileName, String availableStackFilename, long bufferSize) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "trieDirectory");
            if (!root.exists()) {
                root.mkdirs();
            }
            File fTrie = new File(root, trieFileName);
            fTrie.delete();

            FileChannel fcTrie = new RandomAccessFile(fTrie, "rw").getChannel();
            trie = fcTrie.map(FileChannel.MapMode.READ_WRITE, 0, bufferSize * 10);

            File fDel = new File(root, deleteStackFileName);
            fDel.delete();

            FileChannel fcDel = new RandomAccessFile(fDel, "rw").getChannel();
            DeleteTrie = fcDel.map(FileChannel.MapMode.READ_WRITE, 0, 1000);

            File fAvail = new File(root, availableStackFilename);
            fAvail.delete();

            FileChannel fcAvail = new RandomAccessFile(fAvail, "rw").getChannel();
            AvalStack = fcAvail.map(FileChannel.MapMode.READ_WRITE, 0, bufferSize * 4);

            trie.putInt(0, 0);
            trie.putInt(4, 0);
            trie.putChar(8, ' ');

            AvalStack.putInt(0, 0);

            Toast.makeText(MainActivity.this, "Initialization Done", Toast.LENGTH_SHORT).show();
            //System.out.println("Initiailzatioin done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String insertTrie(String s, MappedByteBuffer trie, MappedByteBuffer AvalStack)
            throws FileNotFoundException, IOException, InterruptedException {

        int i;
        int prev_off = -1;
        int curr_off = 0;

        int child, sib;
        char ch;
        sib = trie.getInt(curr_off);
        child = trie.getInt(curr_off + 4);
        ch = trie.getChar(curr_off + 8);

        Node curr = new Node(sib, child, ch);
        //// //System.out.println(s);

        for (i = 0; i < s.length(); i++) {

            if (Math.abs(curr.children) != 0) {

                if (trie.getChar(Math.abs(Math.abs(trie.getInt(curr_off + 4))) + 8) == s.charAt(i)) {
                    //// //System.out.println("character match" + s.charAt(i));

                    curr = getNode(getNode(curr_off).children);
                    prev_off = Math.abs(curr_off);
                    curr_off = Math.abs(getNode(curr_off).children);

                } else {
                    //// //System.out.println("not character match");
                    curr = getNode(getNode(curr_off).children);
                    prev_off = Math.abs(curr_off);
                    curr_off = Math.abs(getNode(curr_off).children);

                    int found_sibling = 0;

                    while (Math.abs(curr.sibling) != 0 && (found_sibling == 0)) {
                        if (trie.getChar(Math.abs(Math.abs(trie.getInt(curr_off)) + 8)) == s.charAt(i)) {

                            found_sibling = 1;
                            curr = getNode(getNode(curr_off).sibling);
                            prev_off = Math.abs(curr_off);
                            curr_off = Math.abs(getNode(curr_off).sibling);
                        } else {
                            curr = getNode(getNode(curr_off).sibling);
                            prev_off = Math.abs(curr_off);
                            curr_off = Math.abs(getNode(curr_off).sibling);

                        }
                    }
                    if (found_sibling == 0) {

                        Node temp = new Node(0, 0, s.charAt(i));
                        curr = temp;
                        int xv = newNodeNumber();

                        trie.putInt(xv, Math.abs(curr.sibling));
                        trie.putInt(xv + 4, Math.abs(curr.children));
                        trie.putChar(xv + 8, curr.ch);
                        prev_off = Math.abs(curr_off);
                        curr_off = xv;
                        trie.putInt(Math.abs(prev_off), xv);

                        // end = end + 10;
                    }

                }
            } else {

                Node temp = new Node(0, 0, s.charAt(i));
                curr = temp;
                int vx = newNodeNumber();

                trie.putInt(vx, Math.abs(curr.sibling));
                trie.putInt(vx + 4, Math.abs(curr.children));
                trie.putChar(vx + 8, curr.ch);

                prev_off = Math.abs(curr_off);
                curr_off = vx;
                trie.putInt(Math.abs(prev_off) + 4, vx);

                // end = end + 10;
            }

        }
        //// //System.out.println(end / 10);

        if (Math.abs(trie.getInt(Math.abs(prev_off) + 4)) == curr_off) {

            if (trie.getInt(Math.abs(prev_off) + 4) < 0)
                return "Already present";//// //System.out.println("previously inserted");
            else {
                trie.putInt(prev_off + 4, -1 * trie.getInt(prev_off + 4));
                return "Inserted";//// //System.out.println("Inserted");
            }
        } else if (Math.abs(Math.abs(trie.getInt(prev_off))) == curr_off) {
            if (trie.getInt(prev_off) < 0)
                return "Already present";//// //System.out.println("previously inserted");
            else {
                trie.putInt(prev_off, -1 * trie.getInt(prev_off));
                return "Inserted";//// //System.out.println("Inserted");
            }

        } else
            return "error";//// //System.out.println("error");

    }

    public String searchTrie(String s, MappedByteBuffer trie)
            throws FileNotFoundException, IOException, InterruptedException {
        int i;
        int prev_off = -1;
        int curr_off = 0;
        int break_flag = 0;

        int child, sib;
        char ch;
        sib = trie.getInt(curr_off);
        child = trie.getInt(curr_off + 4);
        ch = trie.getChar(curr_off + 8);

        Node curr = new Node(sib, child, ch);
// //System.out.println(s);

        for (i = 0; i < s.length(); i++) {

            // //System.out.println(curr.sibling + " " + curr.children + " " + curr.ch + " " +
            // curr_off);

            if (Math.abs(curr.children) != 0) {

                if (trie.getChar(Math.abs(Math.abs(trie.getInt(curr_off + 4))) + 8) == s.charAt(i)) {
                    // //System.out.println("character match" + s.charAt(i));

                    curr = getNode(getNode(curr_off).children);
                    prev_off = Math.abs(curr_off);
                    curr_off = Math.abs(getNode(curr_off).children);

                } else {
                    // //System.out.println("not character match");
                    curr = getNode(getNode(curr_off).children);
                    prev_off = Math.abs(curr_off);
                    curr_off = Math.abs(getNode(curr_off).children);

                    int found_sibling = 0;

                    while (Math.abs(curr.sibling) != 0 && (found_sibling == 0)) {
                        if (trie.getChar(Math.abs(Math.abs(trie.getInt(curr_off)) + 8)) == s.charAt(i)) {
                            // //System.out.println(trie.getChar(Math.abs(trie.getInt(curr_off)) + 8));
                            found_sibling = 1;
                            curr = getNode(getNode(curr_off).sibling);
                            prev_off = Math.abs(curr_off);
                            curr_off = Math.abs(getNode(curr_off).sibling);
                        } else {
                            curr = getNode(getNode(curr_off).sibling);
                            prev_off = Math.abs(curr_off);
                            curr_off = Math.abs(getNode(curr_off).sibling);

                        }
                    }
                    if (found_sibling == 0) {

                        break_flag = 1;
                        break;
                    }

                }
            } else {

                break_flag = 1;
                break;
            }

        }
        if (break_flag == 0) {
            if (Math.abs(trie.getInt(Math.abs(prev_off) + 4)) == curr_off) {

                if (trie.getInt(Math.abs(prev_off) + 4) < 0)
//                    //System.out.println("present");
                    return "Present";
                else {
//                    //System.out.println("not present");
                    return "Not present";
                }
            } else if (Math.abs(Math.abs(trie.getInt(prev_off))) == curr_off) {
                if (trie.getInt(prev_off) < 0)
//                    //System.out.println("present");
                    return "Present";

                else {
//                    trie.putInt(prev_off, -1 * trie.getInt(prev_off));
                    return "Not present";
                }

            }
        } else {
//            System.out.print("Not present");
            return "Not present";
        }

        return "Default";
    }

    public String deleteTrie(String s, MappedByteBuffer trie, MappedByteBuffer DeleteTrie, MappedByteBuffer AvalStack) {

        int i, prev_off = -1, curr_off = 0, break_flag = 0;
        deleteTop = 0;
        int child, sib;
        char ch;
        sib = trie.getInt(curr_off);
        child = trie.getInt(curr_off + 4);
        ch = trie.getChar(curr_off + 8);

        Node curr = new Node(sib, child, ch);
        for (i = 0; i < s.length(); i++) {
            if (Math.abs(curr.children) != 0) {

                if (trie.getChar(Math.abs(Math.abs(trie.getInt(curr_off + 4))) + 8) == s.charAt(i)) {

                    curr = getNode(getNode(curr_off).children);
                    prev_off = Math.abs(curr_off);
                    curr_off = Math.abs(getNode(curr_off).children);

                } else {

                    curr = getNode(getNode(curr_off).children);
                    prev_off = Math.abs(curr_off);
                    curr_off = Math.abs(getNode(curr_off).children);

                    int found_sibling = 0;

                    while (Math.abs(curr.sibling) != 0 && (found_sibling == 0)) {
                        if (trie.getChar(Math.abs(Math.abs(trie.getInt(curr_off)) + 8)) == s.charAt(i)) {
                            found_sibling = 1;
                            curr = getNode(getNode(curr_off).sibling);
                            prev_off = Math.abs(curr_off);
                            curr_off = Math.abs(getNode(curr_off).sibling);
                        } else {
                            curr = getNode(getNode(curr_off).sibling);
                            prev_off = Math.abs(curr_off);
                            curr_off = Math.abs(getNode(curr_off).sibling);

                        }
                    }
                    if (found_sibling == 0) {

                        break_flag = 1;
                        return "String not present";
                    }

                }
            } else {

                break_flag = 1;
                return "String not present";
            }
            if (break_flag == 1) {
                // //System.out.println("String not found");
            }

            deleteNode deleteTemp = new deleteNode(curr_off, prev_off, s.charAt(i));
            deleteTop = deleteTop + 10;
            //// //System.out.println("Written at" + deleteTop + " " + deleteTemp.current_off
            //// +
            // " " + deleteTemp.previous_off
            // + " " + deleteTemp.ch);

            DeleteTrie.putInt(deleteTop, deleteTemp.current_off);
            DeleteTrie.putInt(deleteTop + 4, deleteTemp.previous_off);
            DeleteTrie.putChar(deleteTop + 8, deleteTemp.ch);

        }
        // //System.out.println(deleteTop);

        // //System.out.println(deleteTop);

        // for (i = deleteTop; i >= 10; i = i - 10) {
        // //System.out.println(trie.getInt(DeleteTrie.getInt(i) + 4) + " " +
        // trie.getInt(DeleteTrie.getInt(i + 4) + 4));
        // }
        for (i = deleteTop; i >= 10; i = i - 10) {

            int subStringFlag = 0;
            if (i == deleteTop && getNode(DeleteTrie.getInt(i)).children != 0) {

                //// //System.out.println(getNode(DeleteTrie.getInt(i)).ch);

                if (getNode(DeleteTrie.getInt(i)).children != 0) {
                    if (Math.abs(getNode(DeleteTrie.getInt(i + 4)).children) == DeleteTrie.getInt(i)
                            && trie.getInt(DeleteTrie.getInt(i + 4) + 4) < 0) {
                        trie.putInt((DeleteTrie.getInt(i + 4) + 4),
                                trie.getInt(DeleteTrie.getInt(i + 4) + 4) * -1);
                        subStringFlag = 1;

                    } else if (Math.abs(getNode(DeleteTrie.getInt(i + 4)).sibling) == DeleteTrie.getInt(i)
                            && trie.getInt(DeleteTrie.getInt(i + 4)) < 0) {
                        trie.putInt((DeleteTrie.getInt(i + 4)),
                                trie.getInt(DeleteTrie.getInt(i + 4)) * -1);
                        subStringFlag = 1;

                    }
                }
                if (subStringFlag == 1) {
                    // //System.out.println("String deleted");
                    return "Deleted";
                } else {
                    // //System.out.println("SubString not deleted");
                    return "String Not present";

                }

            } else { //// //System.out.println("hi"); //
                // //System.out.println(DeleteTrie.getInt(i));
                // //System.out.println(getNode(DeleteTrie.getInt(i)).children);
                if (getNode(DeleteTrie.getInt(i)).children == 0) {

                    if (getNode(DeleteTrie.getInt(i)).sibling == 0) {

                        if (Math.abs(getNode(DeleteTrie.getInt(i + 4)).children) == DeleteTrie.getInt(i)) {
                            // //System.out.println("@-----" + trie.getInt(DeleteTrie.getInt(i + 4) + 4));

                            if (trie.getInt(DeleteTrie.getInt(i + 4) + 4) < 0 && i == deleteTop) {

                                trie.putInt(DeleteTrie.getInt(i + 4) + 4, 0);
                                // //System.out.println(getNode(DeleteTrie.getInt(i + 4)).children);

                            } else if (trie.getInt(DeleteTrie.getInt(i + 4) + 4) > 0 && i != deleteTop) {
                                trie.putInt(DeleteTrie.getInt(i + 4) + 4, 0);
                            } else if (trie.getInt(DeleteTrie.getInt(i + 4) + 4) < 0 && i != deleteTop) {
                                // //System.out.println("----------");

                                break;

                            }
                        } else if (Math.abs(getNode(DeleteTrie.getInt(i + 4)).sibling) == DeleteTrie.getInt(i)) {


                            if (trie.getInt(DeleteTrie.getInt(i + 4)) < 0 && i == deleteTop) {
                                trie.putInt(DeleteTrie.getInt(i + 4), 0);
                            } else if (trie.getInt(DeleteTrie.getInt(i + 4)) > 0 && i != deleteTop) {
                                trie.putInt(DeleteTrie.getInt(i + 4), 0);
                            } else if (trie.getInt(DeleteTrie.getInt(i + 4)) < 0 && i != deleteTop) {

                                break;

                            }

                        }

                        availableTop = availableTop + 4;
                        AvalStack.putInt(availableTop, DeleteTrie.getInt(i));
                        trie.putInt(DeleteTrie.getInt(i), 0);
                        trie.putInt(DeleteTrie.getInt(i) + 4, 0);
                        trie.putChar(DeleteTrie.getInt(i) + 8, '~');
                    } else if (getNode(DeleteTrie.getInt(i)).sibling != 0) {

                        if (Math.abs(getNode(DeleteTrie.getInt(i + 4)).sibling) == DeleteTrie.getInt(i)) {

                            if (trie.getInt(DeleteTrie.getInt(i + 4)) < 0 && i == deleteTop) {
                                trie.putInt(DeleteTrie.getInt(i + 4), trie.getInt(DeleteTrie.getInt(i)));
                            } else if (trie.getInt(DeleteTrie.getInt(i + 4)) > 0 && i != deleteTop) {
                                trie.putInt(DeleteTrie.getInt(i + 4), trie.getInt(DeleteTrie.getInt(i)));
                            } else if (trie.getInt(DeleteTrie.getInt(i + 4)) < 0 && i != deleteTop) {
                                // //System.out.println("----------");

                                break;

                            }
                        } else if (Math.abs(getNode(DeleteTrie.getInt(i + 4)).children) == DeleteTrie.getInt(i)) {

                            if (trie.getInt(DeleteTrie.getInt(i + 4) + 4) < 0 && i == deleteTop) {
                                trie.putInt(DeleteTrie.getInt(i + 4) + 4, trie.getInt(DeleteTrie.getInt(i)));
                            } else if (trie.getInt(DeleteTrie.getInt(i + 4) + 4) > 0 && i != deleteTop) {
                                trie.putInt(DeleteTrie.getInt(i + 4) + 4, trie.getInt(DeleteTrie.getInt(i)));
                            } else if (trie.getInt(DeleteTrie.getInt(i + 4) + 4) < 0 && i != deleteTop) {
                                //// //System.out.println("----------");

                                break;

                            }

                        }
                        //// //System.out.println("----------" + availableTop);

                        availableTop = availableTop + 4;
                        //// //System.out.println("----------" + availableTop);

                        AvalStack.putInt(availableTop, DeleteTrie.getInt(i));
                        trie.putInt(DeleteTrie.getInt(i), 0);
                        trie.putInt(DeleteTrie.getInt(i) + 4, 0);
                        trie.putChar(DeleteTrie.getInt(i) + 8, '~');

                    }
                }
            }

        }
        return "Deleted";
    }

    public String prefixMatch(String s, MappedByteBuffer trie) {
        int i;
        int prev_off = -1;
        int curr_off = 0;
        int break_flag = 0;

        int child, sib;
        char ch;
        sib = trie.getInt(curr_off);
        child = trie.getInt(curr_off + 4);
        ch = trie.getChar(curr_off + 8);

        Node curr = new Node(sib, child, ch);
// //System.out.println(s);
        StringBuilder result = new StringBuilder("");
        for (i = 0; i < s.length(); i++) {

            // //System.out.println(curr.sibling + " " + curr.children + " " + curr.ch + " " +
            // curr_off);

            if (Math.abs(curr.children) != 0) {

                if (trie.getChar(Math.abs(Math.abs(trie.getInt(curr_off + 4))) + 8) == s.charAt(i)) {
                    // //System.out.println("character match" + s.charAt(i));

                    curr = getNode(getNode(curr_off).children);
                    prev_off = Math.abs(curr_off);
                    curr_off = Math.abs(getNode(curr_off).children);

                } else {
                    // //System.out.println("not character match");
                    curr = getNode(getNode(curr_off).children);
                    prev_off = Math.abs(curr_off);
                    curr_off = Math.abs(getNode(curr_off).children);

                    int found_sibling = 0;

                    while (Math.abs(curr.sibling) != 0 && (found_sibling == 0)) {
                        if (trie.getChar(Math.abs(Math.abs(trie.getInt(curr_off)) + 8)) == s.charAt(i)) {
                            // //System.out.println(trie.getChar(Math.abs(trie.getInt(curr_off)) + 8));
                            found_sibling = 1;
                            curr = getNode(getNode(curr_off).sibling);
                            prev_off = Math.abs(curr_off);
                            curr_off = Math.abs(getNode(curr_off).sibling);
                        } else {
                            curr = getNode(getNode(curr_off).sibling);
                            prev_off = Math.abs(curr_off);
                            curr_off = Math.abs(getNode(curr_off).sibling);

                        }
                    }
                    if (found_sibling == 0) {

                        break_flag = 1;
                        break;
                    }

                }
            } else {

                break_flag = 1;
                break;
            }
            result.append(s.charAt(i));
        }
        return result.toString();
    }

    public String prefixMatchWord(String s, MappedByteBuffer trie) {
        int i;
        int prev_off = -1;
        int curr_off = 0;
        int break_flag = 0;

        int child, sib;
        char ch;
        sib = trie.getInt(curr_off);
        child = trie.getInt(curr_off + 4);
        ch = trie.getChar(curr_off + 8);

        Node curr = new Node(sib, child, ch);
// //System.out.println(s);
        StringBuilder result = new StringBuilder("");
        StringBuilder temp = new StringBuilder("");
        boolean print = true;
        for (i = 0; i < s.length(); i++) {
            print = true;
            // //System.out.println(curr.sibling + " " + curr.children + " " + curr.ch + " " +
            // curr_off);

            if (Math.abs(curr.children) != 0) {

                if (trie.getChar(Math.abs(Math.abs(trie.getInt(curr_off + 4))) + 8) == s.charAt(i)) {
                    // //System.out.println("character match" + s.charAt(i));
                    if (trie.getInt(curr_off + 4) < 0) {
                        temp.append(s.charAt(i));
                        print = false;
//                        result.append(temp);
                        result = new StringBuilder(temp);
//                        temp.setLength(0);
                    }
                    curr = getNode(getNode(curr_off).children);
                    prev_off = Math.abs(curr_off);
                    curr_off = Math.abs(getNode(curr_off).children);

                } else {
                    // //System.out.println("not character match");
                    curr = getNode(getNode(curr_off).children);
                    prev_off = Math.abs(curr_off);
                    curr_off = Math.abs(getNode(curr_off).children);

                    int found_sibling = 0;

                    while (Math.abs(curr.sibling) != 0 && (found_sibling == 0)) {
                        if (trie.getChar(Math.abs(trie.getInt(curr_off)) + 8) == s.charAt(i)) {
                            // //System.out.println(trie.getChar(Math.abs(trie.getInt(curr_off)) + 8));
                            if (trie.getInt(curr_off) < 0) {
                                temp.append(s.charAt(i));
//                                result.append(temp);
                                result = new StringBuilder(temp);
                                print = false;
//                                temp.setLength(0);
                            }
                            found_sibling = 1;
                            curr = getNode(getNode(curr_off).sibling);
                            prev_off = Math.abs(curr_off);
                            curr_off = Math.abs(getNode(curr_off).sibling);
                        } else {
                            curr = getNode(getNode(curr_off).sibling);
                            prev_off = Math.abs(curr_off);
                            curr_off = Math.abs(getNode(curr_off).sibling);

                        }
                    }
                    if (found_sibling == 0) {

                        break_flag = 1;
                        break;
                    }

                }
            } else {

                break_flag = 1;
                break;
            }
            if (print) {
                temp.append(s.charAt(i));
            }
        }
        return result.toString();
    }

    public int getleaf(String s, MappedByteBuffer trie) {

        int i;

        int prev_off = -1;
        int curr_off = 0;
        int break_flag = 0;

        int child, sib;
        char ch;
        sib = trie.getInt(curr_off);
        child = trie.getInt(curr_off + 4);
        ch = trie.getChar(curr_off + 8);

        Node curr = new Node(sib, child, ch);
        //// //System.out.println(s);

        for (i = 0; i < s.length(); i++) {

            //// //System.out.println(curr.sibling + " " + curr.children + " " + curr.ch + " "
            //// +
            // curr_off);

            if (Math.abs(curr.children) != 0) {

                if (trie.getChar(Math.abs(Math.abs(trie.getInt(curr_off + 4))) + 8) == s.charAt(i)) {
                    //// //System.out.println("character match" + s.charAt(i));

                    curr = getNode(getNode(curr_off).children);
                    prev_off = Math.abs(curr_off);
                    curr_off = Math.abs(getNode(curr_off).children);

                } else {
                    //// //System.out.println("not character match");
                    curr = getNode(getNode(curr_off).children);
                    prev_off = Math.abs(curr_off);
                    curr_off = Math.abs(getNode(curr_off).children);

                    int found_sibling = 0;

                    while (Math.abs(curr.sibling) != 0 && (found_sibling == 0)) {
                        if (trie.getChar(Math.abs(Math.abs(trie.getInt(curr_off)) + 8)) == s.charAt(i)) {
                            //// //System.out.println(trie.getChar(Math.abs(trie.getInt(curr_off)) + 8));
                            found_sibling = 1;
                            curr = getNode(getNode(curr_off).sibling);
                            prev_off = Math.abs(curr_off);
                            curr_off = Math.abs(getNode(curr_off).sibling);
                        } else {
                            curr = getNode(getNode(curr_off).sibling);
                            prev_off = Math.abs(curr_off);
                            curr_off = Math.abs(getNode(curr_off).sibling);

                        }
                    }
                    if (found_sibling == 0) {

                        break_flag = 1;
                        break;
                    }

                }
            } else {

                break_flag = 1;
                break;
            }

        }
//        return Math.abs(curr_off);
        if (break_flag == 0) {
            if (Math.abs(trie.getInt(Math.abs(prev_off) + 4)) == curr_off) {

                return Math.abs(curr_off);

            } else if (Math.abs(Math.abs(trie.getInt(Math.abs(prev_off)))) == curr_off) {

                return Math.abs(curr_off);


            }

        } else {
//            System.out.print("Not present");
            return -1;
        }

        return -1;


    }

    public void queryComplete(int curr_off, int prev_off, String key, StringBuilder result, TreeSet<String> tree_set) {

        while (curr_off != 0) {

            int len = result.length();
            result.append(trie.getChar(Math.abs(curr_off) + 8));

            if ((Math.abs(getNode(Math.abs(prev_off)).children)) == curr_off) {
                if (trie.getInt(prev_off + 4) < 0) {
                    tree_set.add(key + result);
                }
            } else if ((Math.abs(getNode(Math.abs(prev_off)).sibling)) == curr_off) {
                if (trie.getInt(prev_off) < 0) {
                    tree_set.add(key + result);

                }
            }

            if (trie.getInt(curr_off + 4) != 0) {
                queryComplete(Math.abs(trie.getInt(curr_off + 4)), curr_off, key, new StringBuilder(result), tree_set);
            }
            prev_off = curr_off;
            curr_off = Math.abs(trie.getInt(Math.abs(curr_off)));
            result.setLength(result.length() - 1);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//------------------------------------------------------------------------------------
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "trieDirectory");
            if (!root.exists()) {
                initializeTrie("trieFile", "DeleteStack", "AvailableStack", (1027804 + 100));
            } else {
                File fTrie = new File(root, "trieFile");
//                fTrie.delete();

                FileChannel fcTrie = new RandomAccessFile(fTrie, "rw").getChannel();
                trie = fcTrie.map(FileChannel.MapMode.READ_WRITE, 0, (1027804 + 100) * 10);

                File fDel = new File(root, "DeleteStack");
//                fDel.delete();

                FileChannel fcDel = new RandomAccessFile(fDel, "rw").getChannel();
                DeleteTrie = fcDel.map(FileChannel.MapMode.READ_WRITE, 0, 1000);

                File fAvail = new File(root, "AvailableStack");
//                fAvail.delete();

                FileChannel fcAvail = new RandomAccessFile(fAvail, "rw").getChannel();
                AvalStack = fcAvail.map(FileChannel.MapMode.READ_WRITE, 0, (1027804 + 100) * 4);

//
                Toast.makeText(MainActivity.this, "Continued", Toast.LENGTH_SHORT).show();
//                //System.out.println("Initiailzatioin done");
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


//        ----------------------------------------------------------------------------


        initializeButton = findViewById(R.id.initialize_button_id);
        initializeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long nano_startTime = System.nanoTime();
                long millis_startTime = System.currentTimeMillis();
                initializeTrie("trieFile", "DeleteStack", "AvailableStack", (1027804 + 100));

                long nano_endTime = System.nanoTime();
                long millis_endTime = System.currentTimeMillis();
                //System.out.println(nano_endTime-nano_startTime);
                //System.out.println(millis_endTime - millis_startTime);
            }
        });
        insertButton = findViewById(R.id.insert_button_id);
        insertTextView = (TextView) findViewById(R.id.insert_textView);
        insertEditText = (EditText) findViewById(R.id.insert_id);
        insertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    String array[] = insertEditText.getText().toString().split("\n");
                    if (array[0] != "") {
                        String result = "";
                        long nano_startTime = System.nanoTime();
                        long millis_startTime = System.currentTimeMillis();
                        for (int i = 0; i < array.length; i++) {
                            result += insertTrie(array[i], trie, AvalStack);
                            result += "\n";
                            AvalStack.putInt(0, availableTop);
                            trie.putInt(0, end);
                        }
                        long nano_endTime = System.nanoTime();
                        long millis_endTime = System.currentTimeMillis();
                        //System.out.println("------------------" + (nano_endTime - nano_startTime));
                        //System.out.println(millis_endTime - millis_startTime);
                        long x=millis_endTime - millis_startTime;
                        Toast.makeText(MainActivity.this,"Time taken "+x+" ms",Toast.LENGTH_SHORT).show();
                        insertTextView.setText(result);
                    }
//                    Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        searchButton = findViewById(R.id.search_button_id);
        searchTextView = (TextView) findViewById(R.id.insert_textView);
        searchEditText = (EditText) findViewById(R.id.insert_id);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    long nano_startTime = System.nanoTime();
                    long millis_startTime = System.currentTimeMillis();

                    String array[] = searchEditText.getText().toString().split("\n");
                    if (array[0] != "") {
                        String result = "";
                        for (int i = 0; i < array.length; i++) {
                            result += searchTrie(array[i], trie);
                            result += "\n";
                        }
                        long nano_endTime = System.nanoTime();
                        long millis_endTime = System.currentTimeMillis();
//                        System.out.println(nano_endTime-nano_startTime);
//                        System.out.println(millis_endTime - millis_startTime);
                        long x=millis_endTime - millis_startTime;
                        Toast.makeText(MainActivity.this,"Time taken "+x+" ms",Toast.LENGTH_SHORT).show();

                        searchTextView.setText(result);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        DictionaryButton = findViewById(R.id.Dictionary);
        DictionaryButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    Thread thread = new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            BufferedReader reader = null;
                                                            try {
                                                                reader = new BufferedReader(
                                                                        new InputStreamReader(getAssets().open("dictionary")));
                                                                String s;

                                                                long nano_startTime = System.nanoTime();
                                                                long millis_startTime = System.currentTimeMillis();
                                                                while ((s = reader.readLine()) != null) {
                                                                    insertTrie(s, trie, AvalStack);
//                        AvalStack.putInt(0, availableTop);
                                                                    trie.putInt(0, end);
//                        System.gc();

                                                                }
                                                                long nano_endTime = System.nanoTime();
                                                                long millis_endTime = System.currentTimeMillis();
                                                                Toast.makeText(MainActivity.this, "Completed in " + (millis_endTime - millis_startTime) + " ms", Toast.LENGTH_SHORT).show();
                                                            } catch (IOException e) {
                                                            } catch (Exception e) {
                                                            } finally {
                                                                if (reader != null) {
                                                                    try {
                                                                        reader.close();
                                                                    } catch (IOException e) {
                                                                    }
                                                                }
                                                            }

                                                        }
                                                    });
                                                    thread.start();


                                                }
                                            }

        );

//        handler_.sendMessage(Message.obtain(handler_, UPDATE_UI, "Completed"));

        deleteButton = findViewById(R.id.delete_button_id);
        deleteTextView = (TextView) findViewById(R.id.insert_textView);
        deleteEditText = (EditText) findViewById(R.id.insert_id);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long nano_startTime = System.nanoTime();
                long millis_startTime = System.currentTimeMillis();
                String deleteString = deleteEditText.getText().toString();
                if (deleteString.length() != 0) {
                    deleteTextView.setText(deleteTrie(deleteString, trie, DeleteTrie, AvalStack));
                    AvalStack.putInt(0, availableTop);
                }

                long nano_endTime = System.nanoTime();
                long millis_endTime = System.currentTimeMillis();
                //System.out.println(nano_endTime-nano_startTime);
                //System.out.println(millis_endTime - millis_startTime);
            }
        });
        prefixMatchButton = findViewById(R.id.prefix_button_id);
        prefixMatchTextView = (TextView) findViewById(R.id.insert_textView);
        prefixMatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long nano_startTime = System.nanoTime();
                long millis_startTime = System.currentTimeMillis();
                prefixMatchEditText = (EditText) findViewById(R.id.insert_id);
                prefixMatchTextView.setText(prefixMatch(prefixMatchEditText.getText().toString(), trie));

                long nano_endTime = System.nanoTime();
                long millis_endTime = System.currentTimeMillis();
                //System.out.println(nano_endTime-nano_startTime);
                //System.out.println(millis_endTime - millis_startTime);
            }
        });
        prefixMatchWordButton = findViewById(R.id.prefixword_button_id);
        prefixMatchWordTextView = (TextView) findViewById(R.id.insert_textView);
        prefixMatchWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long nano_startTime = System.nanoTime();
                long millis_startTime = System.currentTimeMillis();
                prefixMatchWordEditText = (EditText) findViewById(R.id.insert_id);
                prefixMatchWordTextView.setText(prefixMatchWord(prefixMatchWordEditText.getText().toString(), trie));

                long nano_endTime = System.nanoTime();
                long millis_endTime = System.currentTimeMillis();
                //System.out.println(nano_endTime-nano_startTime);
                //System.out.println(millis_endTime - millis_startTime);
            }
        });


        queryCompleteTextView = (TextView) findViewById(R.id.query_textView);
        queryCompleteEditText = (EditText) findViewById(R.id.query_id);

        TextWatcher fieldValidatorTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence cs, int start, int before, int count) {
                String s = queryCompleteEditText.getText().toString();
                if (s.length() == 0)
                    queryCompleteTextView.setText("");
                else {
                    int leaf = getleaf(s, trie);
                    if (leaf == -1) {
                        queryCompleteTextView.setText("");
                    } else {
                        if (trie.getInt(leaf + 4) != 0) {
                            long nano_startTime = System.nanoTime();
                            long millis_startTime = System.currentTimeMillis();
                            StringBuilder result = new StringBuilder();
                            TreeSet<String> tree_set = new TreeSet<String>(new The_Comparator());
                            queryComplete(Math.abs(trie.getInt(leaf + 4)), Math.abs(leaf), s, result, tree_set);
                            //System.out.println("-----------------------------" + tree_set.size());
                            if (tree_set.size() != 0) {
                                long nano_endTime = System.nanoTime();
                                long millis_endTime = System.currentTimeMillis();
                                String[] results = new String[tree_set.size()];
                                results = tree_set.toArray(results);
//                    results = new String[]{"apple", "ball", "cat"};
                                SpannableStringBuilder spanTxt = new SpannableStringBuilder();

                                for (int i = 0; i < results.length && i < 20; i++) {
                                    spanTxt.append(results[i]);
                                    spanTxt.setSpan(new ClickableSpan() {
                                        @Override
                                        public void updateDrawState(TextPaint ds) {
                                            ds.setColor(ds.linkColor);    // you can use custom color
                                            ds.setUnderlineText(false);    // this remove the underline
                                        }

                                        @Override
                                        public void onClick(View widget) {
                                            TextView tv = (TextView) widget;
                                            Spanned spanned = (Spanned) tv.getText();
                                            queryCompleteEditText = (EditText) findViewById(R.id.query_id);
                                            queryCompleteEditText.setText(spanned.subSequence(spanned.getSpanStart(this), spanned.getSpanEnd(this)));
                                            queryCompleteTextView.setText("");
                                        }
                                    }, spanTxt.length() - results[i].length(), spanTxt.length(), 0);
                                    spanTxt.setSpan(new ForegroundColorSpan(Color.BLACK), spanTxt.length() - results[i].length(), spanTxt.length(), 0);
                                    spanTxt.append("\n");
                                }
                                queryCompleteTextView.setMovementMethod(LinkMovementMethod.getInstance());
                                queryCompleteTextView.setText(spanTxt, TextView.BufferType.SPANNABLE);
//                            onTextChanged(cs, start,  before,count);

                            } else {
                                queryCompleteTextView.setText("");
                                Toast.makeText(MainActivity.this, "tree set 0", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            queryCompleteTextView.setText(s);

                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable sq) {


            }
        };
        queryCompleteEditText.addTextChangedListener(fieldValidatorTextWatcher);

    }

    public void startAsyncTask(View V) {

    }
}


