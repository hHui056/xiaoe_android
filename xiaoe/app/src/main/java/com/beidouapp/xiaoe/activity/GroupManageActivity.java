package com.beidouapp.xiaoe.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.beidouapp.et.ErrorInfo;
import com.beidouapp.et.IFriendsActionListener;
import com.beidouapp.et.client.domain.GroupInfo;
import com.beidouapp.et.client.domain.UserInfo;
import com.beidouapp.xiaoe.R;
import com.beidouapp.xiaoe.adapter.GroupMembersAdapter;
import com.beidouapp.xiaoe.utils.Constans;
import com.beidouapp.xiaoe.view.MyTitle;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 群组管理
 *
 * @author hHui
 */
public class GroupManageActivity extends BaseActivity {

    public static final int HAVE_GROUP = 0x00;//已加入群组

    public static final int NO_GROUP = 0x01;//未加入群组

    public static final int REFRESH_GROUP_MEMBERS = 0x02;//刷新群成员显示

    @BindView(R.id.title_qunzuguanli)
    MyTitle titleQunzuguanli;
    @BindView(R.id.edit_group_name)
    EditText editGroupName;
    @BindView(R.id.grid_group_members)
    GridView gridGroupMembers;
    @BindView(R.id.btn_sure_group)
    Button btnSureGroup;
    Toast toast = null;

    GroupMembersAdapter adapter = null;
    /**
     *
     */
    SweetAlertDialog dialog;
    /**
     * 显示的成员
     */
    ArrayList<String> membersList = new ArrayList<String>();
    /**
     * 群名称
     */
    private String groupName = "";
    /**
     * 群Id
     */
    private String groupId = "";
    /**
     * 是否允许创建群组
     */
    private boolean isCreateGroup = false;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == HAVE_GROUP) {
                editGroupName.setText(groupName);
                editGroupName.setEnabled(false);
                getGroupMembers();
            } else if (msg.what == NO_GROUP) {
                isCreateGroup = true;
                adapter = new GroupMembersAdapter(GroupManageActivity.this, membersList);
                gridGroupMembers.setAdapter(adapter);
            } else if (msg.what == REFRESH_GROUP_MEMBERS) {
                if (adapter == null) {
                    adapter = new GroupMembersAdapter(GroupManageActivity.this, membersList);
                    gridGroupMembers.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }

            }
        }
    };
    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            if (position == membersList.size()) {//点击最后一项添加群成员
                Intent openCameraIntent = new Intent(GroupManageActivity.this, CaptureActivity.class);
                Where_From = "GroupManageActivity";
                startActivityForResult(openCameraIntent, Constans.CODE_1);
            } else {
                dialog = new SweetAlertDialog(GroupManageActivity.this);
                dialog.setContentText(getString(R.string.sure_delete_member));
                dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        removeGroupMember(position);
                        dialog.dismiss();
                    }
                });
                dialog.show();
                dialog.showCancelButton(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_manage);
        ButterKnife.bind(this);
        titleQunzuguanli.setTitle(getString(R.string.qunzuguanli));


        gridGroupMembers.setOnItemClickListener(itemClick);

        initViewData();
    }

    /**
     * 获取群成员列表
     */
    private void getGroupMembers() {
        isdkContext.getGroupMembers(groupId, new IFriendsActionListener() {
            @Override
            public void onResultData(Object data) {
                List<UserInfo> infos = (List<UserInfo>) data;
                for (UserInfo info : infos) {
                    membersList.add(info.getUserid());
                }
                handler.sendEmptyMessage(REFRESH_GROUP_MEMBERS);
            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {

            }
        });
    }

    /**
     * 初始化要显示的数据
     */
    private void initViewData() {
        /**
         * 获取保存的群信息-->若有则不用去获取群信息，只获取群成员信息
         */
        groupName = sp.getString(Constans.Key.GROUP_NAME_KEY, "");
        groupId = sp.getString(Constans.Key.GROUP_ID_KEY, "");

        if (groupName.equals("")) {//本地未保存群组信息--------->获取群列表
            getGroupList();
        } else {
            editGroupName.setText(groupName);
            editGroupName.setEnabled(false);
            getGroupMembers();
        }
    }

    @OnClick(R.id.btn_sure_group)
    public void onClick() {
        if (isCreateGroup) {
            createGroup();
        }
    }

    /**
     * 获取群列表
     */
    public void getGroupList() {
        isdkContext.getGroups(new IFriendsActionListener() {
            @Override
            public void onResultData(Object data) {
                List<GroupInfo> groupInfoList = (List<GroupInfo>) data;
                if (groupInfoList.size() == 0) {//没有加入群组
                    handler.sendEmptyMessage(NO_GROUP);

                } else {
                    GroupInfo info = groupInfoList.get(0);
                    groupName = info.getGroupname();
                    groupId = info.getTopic();
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(Constans.Key.GROUP_NAME_KEY, info.getGroupname());
                    editor.putString(Constans.Key.GROUP_ID_KEY, info.getTopic());
                    editor.commit();

                    handler.sendEmptyMessage(HAVE_GROUP);

                }

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {

            }
        });
    }

    /**
     * 创建群
     */
    void createGroup() {
        final String group = editGroupName.getText().toString().trim();
        if (group == null || group.equals("")) {
            showErrorMessage(getString(R.string.input_null));
            return;
        }
        if (membersList.size() == 0) {
            showErrorMessage(getString(R.string.no_group_members));
            return;
        }
        isdkContext.createGroup(group, membersList, new IFriendsActionListener() {
            @Override
            public void onResultData(Object data) {//-----------创建群成功
                GroupInfo groupInfo = (GroupInfo) data;
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(Constans.Key.GROUP_NAME_KEY, groupInfo.getGroupname());
                editor.putString(Constans.Key.GROUP_ID_KEY, groupInfo.getTopic());
                editor.commit();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(getString(R.string.create_group_success));
                    }
                });
            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                showErrorMessage(getString(R.string.create_group_fail));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Constans.CODE_4 && requestCode == Constans.CODE_1) {
            String uid = data.getStringExtra(Constans.Key.UID_KEY);
            for (String myid : membersList) {
                if (myid.equals(uid)) {
                    showErrorMessage(getString(R.string.member_is_add));
                    return;
                }
            }
            if (!isCreateGroup) {
                addGroupMember(uid);
            } else {
                membersList.add(uid);
                handler.sendEmptyMessage(REFRESH_GROUP_MEMBERS);
            }

        }
    }

    /**
     * 添加群成员
     *
     * @param uid
     */
    void addGroupMember(final String uid) {
        List<String> members = new ArrayList<String>();
        members.add(uid);
        isdkContext.addGroupMembers(groupId, members, new IFriendsActionListener() {
            @Override
            public void onResultData(Object o) {
                //此处不会回调
            }

            @Override
            public void onSuccess() {//添加成功
                membersList.add(uid);
                handler.sendEmptyMessage(REFRESH_GROUP_MEMBERS);
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {//添加失败
                showErrorMessage(getString(R.string.add_member_fail));
            }
        });
    }

    /**
     * 移除群成员
     *
     * @param position
     */
    void removeGroupMember(final int position) {
        List<String> userlist = new ArrayList<String>();
        userlist.add(membersList.get(position));
        isdkContext.removeGroupMembers(groupId, userlist, new IFriendsActionListener() {
            @Override
            public void onResultData(Object o) {//此处不会回调

            }

            @Override
            public void onSuccess() {
                membersList.remove(position);
                handler.sendEmptyMessage(REFRESH_GROUP_MEMBERS);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(getString(R.string.delete_member_success));
                    }
                });
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                showErrorMessage(getString(R.string.delete_member_fail));
            }
        });
    }

    public void showToast(String str) {
        if (toast == null) {
            toast = Toast.makeText(GroupManageActivity.this, str, Toast.LENGTH_SHORT);
        } else {
            toast.setText(str);
        }
        toast.show();
    }
}
