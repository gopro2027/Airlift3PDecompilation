package com.airliftcompany.alp3.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoauth.util.ClientConstants;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.util.CognitoServiceConstants;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public class CognitoService {
    private static final String CLIENT_ID = "3ag75k2sjbm9gb8pd7mgilt6m2";
    private static final String CLIENT_SECRET = "ct82jbbigcr0phpsitsbee27e02c4ashuqinio9qgj97mmuaa11";
    private static final String IDENTIDY_POOL_ID = "us-east-1:08080850-5309-4b27-8ea7-6feebdd9c6e1";
    private static final String POOL_ID = "us-east-1_MaSRCcWgo";
    private static CognitoService sharedInstance;
    private CallbackInterface callback;
    private CognitoUser cognitoUser;
    private Context context;
    private AmazonDynamoDBClient dynamoDBClient;
    private ForgotPasswordContinuation forgotPasswordContinuation;
    private NewPasswordContinuation newPasswordContinuation;
    private String password;
    private CognitoUserPool userPool;
    private CognitoUserSession userSession;
    private String username;
    AuthenticationHandler authenticationHandler = new AuthenticationHandler() { // from class: com.airliftcompany.alp3.utils.CognitoService.5
        @Override // com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
        public void authenticationChallenge(ChallengeContinuation challengeContinuation) {
            if (challengeContinuation.getChallengeName().equals(CognitoServiceConstants.CHLG_TYPE_NEW_PASSWORD_REQUIRED)) {
                CognitoService.this.newPasswordContinuation = (NewPasswordContinuation) challengeContinuation;
                CognitoService.this.callback.completeCallback(AuthResponse.ForceChangePassword);
            }
            Log.e("tag", "authenticationChallenge");
        }

        @Override // com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
        public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice cognitoDevice) {
            Log.e("tag", "onSuccess");
            CognitoService.this.userSession = cognitoUserSession;
            CognitoService.this.callback.completeCallback(AuthResponse.AuthSuccess);
            CognitoService.this.callback = null;
        }

        @Override // com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String str) {
            authenticationContinuation.setAuthenticationDetails(new AuthenticationDetails(str, CognitoService.this.password, (Map<String, String>) null));
            authenticationContinuation.continueTask();
        }

        @Override // com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
        public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
            Log.e("tag", "getMFACode");
        }

        @Override // com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
        public void onFailure(Exception exc) {
            AmazonServiceException amazonServiceException = (AmazonServiceException) exc;
            if (amazonServiceException.getErrorCode().equals("NotAuthorizedException")) {
                CognitoService.this.callback.completeCallback(AuthResponse.IncorrectPassword);
            } else if (amazonServiceException.getErrorCode().equals("InvalidPasswordException")) {
                CognitoService.this.callback.completeCallback(AuthResponse.InvalidPassword);
            } else if (amazonServiceException.getErrorCode().equals("TooManyFailedAttemptsException")) {
                CognitoService.this.callback.completeCallback(AuthResponse.TooManyFailedAttempts);
            } else if (amazonServiceException.getErrorCode().equals("UserNotFoundException")) {
                CognitoService.this.callback.completeCallback(AuthResponse.UserNotFound);
            } else if (amazonServiceException.getErrorCode().equals("PasswordResetRequiredException")) {
                CognitoService.this.callback.completeCallback(AuthResponse.ResetPasswordRequired);
            } else {
                CognitoService.this.callback.completeCallback(AuthResponse.UnknownError);
            }
            Log.e("tag", exc.getLocalizedMessage());
        }
    };
    ForgotPasswordHandler forgotPasswordHandler = new ForgotPasswordHandler() { // from class: com.airliftcompany.alp3.utils.CognitoService.6
        @Override // com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler
        public void onSuccess() {
            Log.e("tag", "onSuccess");
            CognitoService.this.callback.completeCallback(AuthResponse.AuthSuccess);
        }

        @Override // com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler
        public void getResetCode(ForgotPasswordContinuation forgotPasswordContinuation) {
            Log.e("tag", "getResetCode");
            CognitoService.this.forgotPasswordContinuation = forgotPasswordContinuation;
            CognitoService.this.callback.completeCallback(AuthResponse.AuthSuccess);
        }

        @Override // com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler
        public void onFailure(Exception exc) {
            Log.e("tag", exc.getLocalizedMessage());
            AmazonServiceException amazonServiceException = (AmazonServiceException) exc;
            if (amazonServiceException.getErrorCode().equals("LimitExceededException")) {
                CognitoService.this.callback.completeCallback(AuthResponse.TooManyFailedAttempts);
                return;
            }
            if (amazonServiceException.getErrorCode().equals("UserNotFoundException")) {
                CognitoService.this.callback.completeCallback(AuthResponse.UserNotFound);
                return;
            }
            if (amazonServiceException.getErrorCode().equals("CodeDeliveryFailureException")) {
                CognitoService.this.callback.completeCallback(AuthResponse.CodeDeliveryFailure);
                return;
            }
            if (amazonServiceException.getErrorCode().equals("CodeMismatchException")) {
                CognitoService.this.callback.completeCallback(AuthResponse.IncorrectCode);
                return;
            }
            if (amazonServiceException.getErrorCode().equals("ExpiredCodeException")) {
                CognitoService.this.callback.completeCallback(AuthResponse.ExpiredCode);
                return;
            }
            if (amazonServiceException.getErrorCode().equals("InvalidParameterException")) {
                CognitoService.this.callback.completeCallback(AuthResponse.IncorrectPassword);
                return;
            }
            if (amazonServiceException.getErrorCode().equals("InvalidPasswordException")) {
                CognitoService.this.callback.completeCallback(AuthResponse.IncorrectPassword);
            } else if (amazonServiceException.getErrorCode().equals("TooManyFailedAttemptsException")) {
                CognitoService.this.callback.completeCallback(AuthResponse.TooManyFailedAttempts);
            } else {
                CognitoService.this.callback.completeCallback(AuthResponse.UnknownError);
            }
        }
    };

    public enum AuthResponse {
        AuthSuccess,
        IncorrectPassword,
        InvalidPassword,
        ForceChangePassword,
        ResetPasswordRequired,
        ExpiredCode,
        IncorrectCode,
        TooManyFailedAttempts,
        UserNotFound,
        CodeDeliveryFailure,
        UnknownError,
        AccessDenied
    }

    public interface CallbackInterface {
        void completeCallback(AuthResponse authResponse);
    }

    public static CognitoService getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new CognitoService();
        }
        return sharedInstance;
    }

    public String currentUsername() {
        return this.cognitoUser.getUserId();
    }

    private CognitoUserPool getUserPool() {
        CognitoUserPool cognitoUserPool = this.userPool;
        if (cognitoUserPool != null) {
            return cognitoUserPool;
        }
        CognitoUserPool cognitoUserPool2 = new CognitoUserPool(this.context, POOL_ID, CLIENT_ID, CLIENT_SECRET, Regions.US_EAST_1);
        this.userPool = cognitoUserPool2;
        return cognitoUserPool2;
    }

    public AmazonDynamoDBClient getDynamoDBClient(Context context) {
        AmazonDynamoDBClient amazonDynamoDBClient = this.dynamoDBClient;
        if (amazonDynamoDBClient != null) {
            return amazonDynamoDBClient;
        }
        CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider = new CognitoCachingCredentialsProvider(context, IDENTIDY_POOL_ID, Regions.US_EAST_1);
        HashMap hashMap = new HashMap();
        hashMap.put("cognito-idp.us-east-1.amazonaws.com/us-east-1_MaSRCcWgo", this.userSession.getIdToken().getJWTToken());
        cognitoCachingCredentialsProvider.setLogins(hashMap);
        AmazonDynamoDBClient amazonDynamoDBClient2 = new AmazonDynamoDBClient(cognitoCachingCredentialsProvider);
        this.dynamoDBClient = amazonDynamoDBClient2;
        return amazonDynamoDBClient2;
    }

    public void checkDeviceAuthorization(final String str, final String str2, final Context context, final CallbackInterface callbackInterface) {
        AsyncTask.execute(new Runnable() { // from class: com.airliftcompany.alp3.utils.CognitoService.1
            @Override // java.lang.Runnable
            public void run() {
                try {
                    Log.i("CognitoService", "checkDeviceAuthorization");
                    if (!Util.internetConnection(context)) {
                        callbackInterface.completeCallback(AuthResponse.UnknownError);
                        return;
                    }
                    List<Map<String, AttributeValue>> items = new AmazonDynamoDBClient(new CognitoCachingCredentialsProvider(context, CognitoService.IDENTIDY_POOL_ID, Regions.US_EAST_1)).query(new QueryRequest().withTableName("Devices").withConsistentRead(Boolean.TRUE).withKeyConditions(ImmutableMap.m67of("Serial", new Condition().withComparisonOperator(ComparisonOperator.EQ).withAttributeValueList(new AttributeValue(str))))).getItems();
                    if (items.size() == 0) {
                        callbackInterface.completeCallback(AuthResponse.AccessDenied);
                        return;
                    }
                    Map<String, AttributeValue> map = items.get(0);
                    if (map != null) {
                        AttributeValue attributeValue = map.get("Serial");
                        String s = attributeValue != null ? attributeValue.getS() : "";
                        AttributeValue attributeValue2 = map.get("MacAddress");
                        String s2 = attributeValue2 != null ? attributeValue2.getS() : "";
                        AttributeValue attributeValue3 = map.get("Authorized");
                        boolean booleanValue = attributeValue3 != null ? attributeValue3.getBOOL().booleanValue() : false;
                        if (s2 != null && s2.length() != 0) {
                            if (str.equals(s) && str2.equals(s2) && booleanValue) {
                                callbackInterface.completeCallback(AuthResponse.AuthSuccess);
                                return;
                            }
                        }
                        if (str.equals(s) && booleanValue) {
                            callbackInterface.completeCallback(AuthResponse.AuthSuccess);
                            return;
                        }
                    }
                    callbackInterface.completeCallback(AuthResponse.AccessDenied);
                } catch (Exception e) {
                    Log.e(ClientConstants.DOMAIN_QUERY_PARAM_ERROR, e.getLocalizedMessage());
                    callbackInterface.completeCallback(AuthResponse.UnknownError);
                }
            }
        });
    }

    public void login(String str, String str2, Context context, CallbackInterface callbackInterface) {
        this.callback = callbackInterface;
        this.context = context;
        this.username = str;
        this.password = str2;
        CognitoUser user = getUserPool().getUser(this.username);
        this.cognitoUser = user;
        if (user == null) {
            this.callback.completeCallback(AuthResponse.UserNotFound);
            this.callback = null;
        } else {
            user.signOut();
            this.cognitoUser.getSessionInBackground(this.authenticationHandler);
        }
    }

    public void logout() {
        CognitoUser cognitoUser = this.cognitoUser;
        if (cognitoUser != null) {
            cognitoUser.signOut();
        }
    }

    public void changePassword(String str, final String str2, Context context, CallbackInterface callbackInterface) {
        this.callback = callbackInterface;
        this.context = context;
        if (!str.equals(this.password)) {
            this.callback.completeCallback(AuthResponse.IncorrectPassword);
            this.callback = null;
        } else if (this.cognitoUser == null) {
            this.callback.completeCallback(AuthResponse.UnknownError);
            this.callback = null;
        } else {
            AsyncTask.execute(new Runnable() { // from class: com.airliftcompany.alp3.utils.CognitoService.2
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        CognitoService.this.newPasswordContinuation.setPassword(str2);
                        CognitoService.this.newPasswordContinuation.continueTask();
                    } catch (Exception e) {
                        Log.e(ClientConstants.DOMAIN_QUERY_PARAM_ERROR, e.getLocalizedMessage());
                    }
                }
            });
        }
    }

    public void resetPassword(String str, Context context, CallbackInterface callbackInterface) {
        this.callback = callbackInterface;
        this.context = context;
        CognitoUser user = getUserPool().getUser(str);
        this.cognitoUser = user;
        if (user == null) {
            this.callback.completeCallback(AuthResponse.UserNotFound);
            this.callback = null;
        } else {
            AsyncTask.execute(new Runnable() { // from class: com.airliftcompany.alp3.utils.CognitoService.3
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        CognitoService.this.cognitoUser.forgotPassword(CognitoService.this.forgotPasswordHandler);
                    } catch (Exception e) {
                        Log.e(ClientConstants.DOMAIN_QUERY_PARAM_ERROR, e.getLocalizedMessage());
                    }
                }
            });
        }
    }

    public void confirmForgottenPassword(final String str, final String str2, Context context, CallbackInterface callbackInterface) {
        this.callback = callbackInterface;
        this.context = context;
        if (this.cognitoUser == null) {
            callbackInterface.completeCallback(AuthResponse.UnknownError);
            this.callback = null;
        } else {
            AsyncTask.execute(new Runnable() { // from class: com.airliftcompany.alp3.utils.CognitoService.4
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        CognitoService.this.cognitoUser.confirmPassword(str, str2, CognitoService.this.forgotPasswordHandler);
                    } catch (Exception e) {
                        Log.e(ClientConstants.DOMAIN_QUERY_PARAM_ERROR, e.getLocalizedMessage());
                    }
                }
            });
        }
    }
}
