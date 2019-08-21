package org.liran.noam.ykpassdroid;

import android.app.PendingIntent;
import android.app.assist.AssistStructure;
import android.content.Intent;
import android.content.IntentSender;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.FillCallback;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;
import android.support.annotation.NonNull;
import android.view.autofill.AutofillId;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.google.common.base.Strings;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class YkpassAutofillService extends AutofillService {
    public static final List<String> BLACKLISTED_PACKAGES = Arrays.asList(
            YkpassAutofillService.class.getPackage().getName()
    );

    @Override
    public void onFillRequest(@NonNull FillRequest request, @NonNull CancellationSignal cancellationSignal, @NonNull FillCallback callback) {
        List<AssistStructure.ViewNode> viewNodes = request.getFillContexts().stream()
                .map(FillContext::getStructure)
                .flatMap(assistStructure -> IntStream.range(0, assistStructure.getWindowNodeCount()).mapToObj(assistStructure::getWindowNodeAt))
                .flatMap(viewNode -> flattenViewNodes(viewNode.getRootViewNode()))
                .collect(Collectors.toList());

        List<String> packagesList = viewNodes.stream()
                .map(AssistStructure.ViewNode::getIdPackage)
                .filter(s -> !Strings.isNullOrEmpty(s))
                .collect(Collectors.toList());

        if (packagesList.stream().anyMatch(BLACKLISTED_PACKAGES::contains)) {
            return;
        }

        String idPackage = packagesList.stream()
                .map(s -> StringUtils.reverseDelimited(s, '.'))
                .findFirst().orElse(null);

        String passwordId = viewNodes.stream()
                .map(AssistStructure.ViewNode::getWebDomain)
                .filter(s -> !Strings.isNullOrEmpty(s))
                .findFirst().orElse(idPackage);

        AutofillId passwordAutofillId = viewNodes.stream()
                .filter(n -> n.getClassName() != null)
                .filter(n -> n.getClassName().equals(EditText.class.getName()))
                .filter(n -> (n.getAutofillHints() != null && n.getAutofillHints().length > 0 && n.getAutofillHints()[0].contains("password")) ||
                        (n.getIdEntry() != null && n.getIdEntry().contains("password")) ||
                        isPasswordInputType(n.getInputType()))
                .map(AssistStructure.ViewNode::getAutofillId)
                .findFirst().orElse(null);

        if (passwordAutofillId == null) {
            callback.onFailure("Can't find password field to autofill");
            return;
        }

        Intent authIntent = new Intent(this, MainActivity.class);
        // Send any additional data required to complete the request.
        authIntent.putExtra("autofill", true);
        authIntent.putExtra("autofillId", passwordAutofillId);

        RemoteViews authPresentation = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
        if (passwordId != null) {
            String cleanPasswordId = passwordId.replaceAll("\\.co\\.|\\.gov\\.|www\\.", "");
            String[] parts = cleanPasswordId.split("\\.");
            passwordId = parts.length > 1 ? parts[parts.length - 2] : parts[parts.length - 1];

            authIntent.putExtra("passwordId", passwordId);
            authPresentation.setTextViewText(android.R.id.text1, String.format("ykpass: %s", passwordId));
        } else {
            authPresentation.setTextViewText(android.R.id.text1, "ykpass");
        }

        IntentSender intentSender = PendingIntent.getActivity(
                this,
                1001,
                authIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        ).getIntentSender();

        AutofillId[] ids = new AutofillId[] { passwordAutofillId };
        FillResponse fillResponse = new FillResponse.Builder()
                .setAuthentication(ids, intentSender, authPresentation)
                .build();

        callback.onSuccess(fillResponse);
    }

    private Stream<AssistStructure.ViewNode> flattenViewNodes(AssistStructure.ViewNode viewNode) {
        return Stream.concat(Stream.of(viewNode),
                IntStream.range(0, viewNode.getChildCount()).mapToObj(viewNode::getChildAt).flatMap(this::flattenViewNodes));
    }

    @Override
    public void onSaveRequest(@NonNull SaveRequest request, @NonNull SaveCallback callback) {

    }

    // Taken from TextView.java
    static boolean isPasswordInputType(int inputType) {
        final int variation =
                inputType & (EditorInfo.TYPE_MASK_CLASS | EditorInfo.TYPE_MASK_VARIATION);
        return variation
                == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD)
                || variation
                == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD)
                || variation
                == (EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD);
    }
}