package vn.axonactive.aevent.fragment;

import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.axonactive.aevent.BuildConfig;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.activity.MainActivity;
import vn.axonactive.aevent.api.FileUploadService;
import vn.axonactive.aevent.api.RestfulAPI;
import vn.axonactive.aevent.api.UserEndPointInterface;
import vn.axonactive.aevent.model.User;
import vn.axonactive.aevent.util.DataStorage;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static vn.axonactive.aevent.util.Permissions.checkHasPermission;
import static vn.axonactive.aevent.util.Validation.isEmail;
import static vn.axonactive.aevent.util.Validation.isPhoneNumber;

/**
 * Created by ltphuc on 2/16/2017.
 */
public class ProfileFragment extends Fragment {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CHOOSE_IMAGE_ACTIVITY_REQUEST_CODE = 101;
    private static final int CROP_IMAGE_ACTIVITY_REQUEST_CODE = 102;

    ImageView mImgAvatar;
    EditText mEdtName;
    TextInputLayout mLayoutName;
    RadioButton mRbMale;
    RadioButton mRbFemale;
    EditText mEdtEmail;
    TextInputLayout mLayoutEmail;
    EditText mEdtBirthday;
    EditText mEdtPhone;
    TextInputLayout mLayoutPhone;
    EditText mEdtAddress;
    EditText mEdtJob;
    EditText mEdtCompany;

    private Uri uri;
    private List<EditText> edits = new ArrayList<>();
    private User user;
    private boolean editing = true;
    private SimpleDateFormat dateFormat;
    private MainActivity activity;
    private String mCurrentPath;

    public ProfileFragment() {
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        activity = (MainActivity) getActivity();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mImgAvatar = (ImageView) view.findViewById(R.id.avatar);
        mEdtName = (EditText) view.findViewById(R.id.input_name);
        mLayoutName = (TextInputLayout) view.findViewById(R.id.input_layout_name);
        mRbMale = (RadioButton) view.findViewById(R.id.radio_male);
        mRbFemale = (RadioButton) view.findViewById(R.id.radio_female);
        mEdtEmail = (EditText) view.findViewById(R.id.input_email);
        mLayoutEmail = (TextInputLayout) view.findViewById(R.id.input_layout_email);
        mEdtBirthday = (EditText) view.findViewById(R.id.input_birthday);
        mEdtPhone = (EditText) view.findViewById(R.id.input_phone);
        mLayoutPhone = (TextInputLayout) view.findViewById(R.id.input_layout_phone);
        mEdtAddress = (EditText) view.findViewById(R.id.input_address);
        mEdtJob = (EditText) view.findViewById(R.id.input_job);
        mEdtCompany = (EditText) view.findViewById(R.id.input_company);

        mImgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeAvatar(mImgAvatar);
            }
        });

        mEdtBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectBirthday();
            }
        });

        initViewComponents();

        RestfulAPI restfulAPI = new RestfulAPI();
        Retrofit retrofit = restfulAPI.getRestClient();
        UserEndPointInterface userApiService = retrofit.create(UserEndPointInterface.class);
        SharedPreferences prefs = getContext().getSharedPreferences(DataStorage.APP_PREFS, MODE_PRIVATE);
        String token = prefs.getString(DataStorage.TOKEN, null);
        if (token != null) {
            activity.showLoadingDialog();
            Call<User> call = userApiService.getUser(token);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, final Response<User> response) {
                    if (response != null) {
                        int statusCode = response.code();
                        if (statusCode == HttpURLConnection.HTTP_OK) {
                            user = response.body();

                            String fullName = user.getFullName();
                            String accountCode = user.getAccountCode();
                            String urlAvatar = user.getAvatar();
                            String accountType = user.getAccountType();
                            String email = user.getEmail();
                            DataStorage.email = email;

                            activity.setUserCorner(email, fullName, urlAvatar, accountCode, accountType);
                            loadUser(user);
                        } else {
                            Toast.makeText(getContext(), getString(R.string.err_network_connection), Toast.LENGTH_SHORT).show();
                        }
                        activity.hideLoadingDialog();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Toast.makeText(getContext(), getString(R.string.err_network_connection), Toast.LENGTH_SHORT).show();
                    activity.hideLoadingDialog();
                }
            });
        }
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_profile, menu);

        MenuItem miEdit = menu.findItem(R.id.action_edit);

        if (miEdit != null) {
            miEdit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    if (editing) {
                        enableUserInput(true);
                        item.setIcon(R.drawable.ic_check);
                        activity.setTitle("Edit Profile");
                        editing = !editing;
                    } else {
                        if (isValidInput()) {
                            saveUser(user);
                            enableUserInput(false);
                            editing = !editing;
                            item.setIcon(R.drawable.ic_edit);
                            closeKeyboard();
                            activity.setTitle("Profile");
                        }
                    }

                    return true;
                }
            });
        }

    }

    private void initViewComponents() {
        edits.add(mEdtName);
        edits.add(mEdtEmail);
        edits.add(mEdtPhone);
        edits.add(mEdtAddress);
        edits.add(mEdtJob);
        edits.add(mEdtCompany);
        enableUserInput(false);
    }

    public void changeAvatar(View v) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                switch (itemId) {
                    case R.id.item_take_camera: {
                        if (checkHasPermission(getContext(), android.Manifest.permission.CAMERA)) {
                            captureImage();
                            return true;
                        } else {
                            makeToast("Permission to access camera not granted");
                            return false;
                        }
                    }
                    case R.id.item_choose_picture: {
                        if (checkHasPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                & checkHasPermission(getContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            chooseImage();
                            return true;
                        } else {
                            makeToast("Permission to access storage not granted");
                            return false;
                        }
                    }
                }
                return true;
            }
        });
        popupMenu.inflate(R.menu.popup_menu_avatar);
        popupMenu.show();
    }

    private void selectBirthday() {
        if (mEdtBirthday.isClickable()) {
            closeKeyboard();
            createDatePickerDialog();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void saveUser(User user) {
        user.setFullName(mEdtName.getText().toString().trim());
        if (mRbFemale.isChecked()) {
            user.setGender(0);
        } else {
            user.setGender(1);
        }
        user.setEmail(mEdtEmail.getText().toString());
        try {
            user.setBirthday(dateFormat.parse(mEdtBirthday.getText().toString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        user.setPhone(mEdtPhone.getText().toString().trim());
        user.setAddress(mEdtAddress.getText().toString().trim());
        user.setJob(mEdtJob.getText().toString().trim());
        user.setCompany(mEdtCompany.getText().toString().trim());
        uploadUser(user);
        loadUser(user);
    }

    private void uploadUser(User user) {
        RestfulAPI restfulAPI = new RestfulAPI();
        Retrofit retrofit = restfulAPI.getRestClient();
        UserEndPointInterface userApiService = retrofit.create(UserEndPointInterface.class);
        SharedPreferences prefs = getContext().getSharedPreferences(DataStorage.APP_PREFS, MODE_PRIVATE);
        String token = prefs.getString(DataStorage.TOKEN, null);
        if (token != null) {
            Call<User> call = userApiService.updateUser(token, user);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    Toasty.success(getContext(), "Successfully saved").show();
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    makeToast("An error has occurred, please try again");
                }
            });
        }
    }

    private void loadUser(User user) {

        if (user != null) {

            mEdtName.setText(user.getFullName());
            mEdtEmail.setText(user.getEmail());
            if (user.getGender() == 0) {
                mRbFemale.setChecked(true);
            } else {
                mRbMale.setChecked(true);
            }
            Date birthday = user.getBirthday();
            if (birthday != null) {
                mEdtBirthday.setText(dateFormat.format(birthday));
            }
            mEdtPhone.setText(user.getPhone());
            mEdtAddress.setText(user.getAddress());
            mEdtJob.setText(user.getJob());
            mEdtCompany.setText(user.getCompany());
            String accountType = user.getAccountType();
            String urlImage;
            if ("system".equals(accountType)) {
                urlImage = BuildConfig.API_URL + user.getAvatar();
            } else {
                urlImage = user.getAvatar();
                mImgAvatar.setClickable(false);
            }
            if (!"".equals(urlImage)) {
                Picasso.with(getContext())
                        .load(urlImage)
                        .placeholder(R.mipmap.avt_default)
                        .into(mImgAvatar);
            }
        }
    }

    private void enableUserInput(boolean bool) {
        if (bool) {
            makeToast("Now Editing");
        } else {
            mLayoutName.setErrorEnabled(false);
            mLayoutEmail.setErrorEnabled(false);
            mLayoutPhone.setErrorEnabled(false);
        }
        for (EditText edit : edits) {
            edit.setFocusable(bool);
            edit.setFocusableInTouchMode(bool);
        }
        mEdtBirthday.setClickable(bool);
        mRbFemale.setClickable(bool);
        mRbMale.setClickable(bool);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public boolean isValidInput() {
        return validateName() & validateEmail() & validatePhone() & validateInput(mEdtJob, 100)
                & validateInput(mEdtCompany, 100) & validateInput(mEdtAddress, 254);
    }

    private boolean validateInput(EditText editText, int maxLength) {
        if (editText.getText().toString().trim().length() >= maxLength) {
            return false;
        }
        return true;
    }

    private boolean validateName() {
        if (mEdtName.getText().toString().trim().isEmpty() || (mEdtName.getText().toString().trim().length() >= 70)) {
            mLayoutName.setError(getString(R.string.err_msg_name));
            return false;
        } else {
            mLayoutName.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateEmail() {
        String email = mEdtEmail.getText().toString().trim();
        if ((email.isEmpty() || !isEmail(email)) || (email.length() >= 70)) {
            mLayoutEmail.setError(getString(R.string.err_msg_email));
            return false;
        } else {
            mLayoutEmail.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validatePhone() {
        if (!isPhoneNumber(mEdtPhone.getText().toString().trim())) {
            mLayoutPhone.setError(getString(R.string.err_msg_phone_invalid));
            return false;
        } else {
            mLayoutPhone.setErrorEnabled(false);
        }
        return true;
    }

    private void createDatePickerDialog() {
        DatePickerDialog birthdayDPD;
        Calendar newCal = Calendar.getInstance();
        birthdayDPD = new DatePickerDialog(getContext(), new HandleDate(), newCal.get(Calendar.YEAR),
                newCal.get(Calendar.MONTH), newCal.get(Calendar.DAY_OF_MONTH));
        birthdayDPD.getDatePicker().setMaxDate(new Date().getTime());
        birthdayDPD.show();
    }

    private class HandleDate implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, dayOfMonth);
            mEdtBirthday.setText(dateFormat.format(newDate.getTime()));
        }
    }

    private void makeToast(String s) {
        Toasty.normal(getContext(), s).show();
    }

    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    private void uploadFile(File file) {
        activity.showLoadingDialog();
        RestfulAPI restfulAPI = new RestfulAPI();
        Retrofit retrofit = restfulAPI.getRestClient();
        FileUploadService service =
                retrofit.create(FileUploadService.class);
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", file.getName(), reqFile);
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "Upload avatar");
        SharedPreferences prefs = getActivity().getSharedPreferences(DataStorage.APP_PREFS, Context.MODE_PRIVATE);
        String token = prefs.getString(DataStorage.TOKEN, null);
        Call<ResponseBody> call = service.upload(token, body, name);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                if (response.body() != null) {
                    try {
                        String url = response.body().string();
                        user.setAvatar(url);

                        activity.setUserCorner(user.getEmail(), user.getFullName(), user.getAvatar(),
                                user.getAccountCode(), user.getAccountType());

                        Picasso.with(getContext())
                                .load(BuildConfig.API_URL + url)
                                .into(mImgAvatar);

                        Toasty.success(getContext(), "Successfully").show();
                        activity.hideLoadingDialog();
                    } catch (IOException e) {
                        makeToast(getString(R.string.err_unknown));
                        activity.hideLoadingDialog();
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                activity.hideLoadingDialog();
                makeToast(getString(R.string.err_unknown));
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE: {
                    cropImage();
                    break;
                }
                case CHOOSE_IMAGE_ACTIVITY_REQUEST_CODE: {
                    if (data != null) {
                        uri = data.getData();
                        cropImage();
                    }
                    break;
                }
                case CROP_IMAGE_ACTIVITY_REQUEST_CODE: {
                    File file = new File(mCurrentPath);
                    uploadFile(file);
                    break;
                }
            }
        }
    }

    private void cropImage() {
        try {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                Intent cropIntent = new Intent("com.android.camera.action.CROP");
                cropIntent.setDataAndType(uri, "image/*");
                Uri photoUri = FileProvider.getUriForFile(activity,
                        getString(R.string.fileProvider),
                        photoFile);
                mCurrentPath = photoFile.getAbsolutePath();
                cropIntent.putExtra("crop", "true");
                cropIntent.putExtra("aspectX", 1);
                cropIntent.putExtra("aspectY", 1);
                cropIntent.putExtra("outputX", 600);
                cropIntent.putExtra("outputY", 600);
                cropIntent.putExtra("return-data", true);
                cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                List<ResolveInfo> resInfoList = getContext().getPackageManager().queryIntentActivities(cropIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    getContext().grantUriPermission(packageName, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                startActivityForResult(cropIntent, CROP_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        } catch (ActivityNotFoundException anfe) {
            makeToast("Whoops - your device doesn't support the crop action!");
        }
    }

    private void captureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                makeToast(ex.toString());
            }
            if (photoFile != null) {
                uri = FileProvider.getUriForFile(activity,
                        getString(R.string.fileProvider),
                        photoFile);
                List<ResolveInfo> resInfoList = getContext().getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    getContext().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    private void chooseImage() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
        startActivityForResult(chooserIntent, CHOOSE_IMAGE_ACTIVITY_REQUEST_CODE);
    }
}