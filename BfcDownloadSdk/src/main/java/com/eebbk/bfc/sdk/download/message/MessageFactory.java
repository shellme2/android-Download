package com.eebbk.bfc.sdk.download.message;

import com.eebbk.bfc.sdk.download.DownloadInnerTask;

/**
 * Desc: 消息工厂
 * Author: llp
 * Create Time: 2016-10-22 20:29
 * Email: jacklulu29@gmail.com
 */

public class MessageFactory {

    private MessageFactory(){
        // private construct
    }

    public static DownloadBaseMessage createDownloadWaitingMsg(DownloadInnerTask task){
        return createBaseMsg(task);
    }

    public static DownloadBaseMessage createDownloadStartedMsg(DownloadInnerTask task){
        return createBaseMsg(task);
    }

    public static DownloadBaseMessage createDownloadConnectedMsg(DownloadInnerTask task, boolean resuming){
        return new ConnectedMessage(
                task.getGenerateId(),
                task.getModuleName(),
                task.getState(),
                resuming,
                task.getTotalSize(),
                task.getFinishSize(),
                task.getFileName(),
                task.getFileExtension()
                );
    }

    public static DownloadBaseMessage createDownloadingMsg(DownloadInnerTask task){
        return createProgressMsg(task);
    }

    public static DownloadBaseMessage createDownloadPauseMsg(DownloadInnerTask task){
        return createErrorMsg(task);
    }

    public static DownloadBaseMessage createDownloadRetryMsg(DownloadInnerTask task){
        return createErrorMsg(task);
    }

    public static DownloadBaseMessage createDownloadFailureMsg(DownloadInnerTask task){
        return createErrorMsg(task);
    }

    public static DownloadBaseMessage createDownloadSuccessMsg(DownloadInnerTask task){
        return createSizeMsg(task);
    }

    public static DownloadBaseMessage createDownloadRestartMsg(DownloadInnerTask task){
        return createSizeMsg(task);
    }

    public static DownloadBaseMessage createCheckStartedMsg(DownloadInnerTask task){
        return createSizeMsg(task);
    }

    public static DownloadBaseMessage createCheckingMsg(DownloadInnerTask task){
        return createProgressMsg(task);
    }

    public static DownloadBaseMessage createCheckFailureMsg(DownloadInnerTask task){
        return createErrorMsg(task);
    }

    public static DownloadBaseMessage createCheckSuccessMsg(DownloadInnerTask task){
        return createSizeMsg(task);
    }

    public static DownloadBaseMessage createUnpackStartedMsg(DownloadInnerTask task){
        return createSizeMsg(task);
    }

    public static DownloadBaseMessage createUnpackingMsg(DownloadInnerTask task){
        return createProgressMsg(task);
    }

    public static DownloadBaseMessage createUnpackFailureMsg(DownloadInnerTask task){
        return createErrorMsg(task);
    }

    public static DownloadBaseMessage createUnpackSuccessMsg(DownloadInnerTask task){
        return createSizeMsg(task);
    }

    private static DownloadBaseMessage createBaseMsg(DownloadInnerTask task){
        return new DownloadBaseMessage(task.getGenerateId(), task.getModuleName(), task.getState());
    }

    public static DownloadBaseMessage createConfigChangedMsg(int id, String moduleName, int networkTypes){
        return new ConfigChangedMsg( id, moduleName, networkTypes);
    }

    public static DownloadBaseMessage createOperationMsg(DownloadInnerTask task, int operation){
        return new OperationMsg(task.getGenerateId(), task.getModuleName(), operation);
    }

    private static DownloadBaseMessage createSizeMsg(DownloadInnerTask task){
        return new SizeMsg(
                task.getGenerateId(),
                task.getModuleName(),
                task.getState(),
                task.getTotalSize(),
                task.getFinishSize()
        );
    }

    private static DownloadBaseMessage createProgressMsg(DownloadInnerTask task){
        return new
                ProgressMsg(
                task.getGenerateId(),
                task.getModuleName(),
                task.getState(),
                task.getTotalSize(),
                task.getFinishSize(),
                task.getSpeed()
        );
    }

    private static DownloadBaseMessage createErrorMsg(DownloadInnerTask task){
        return new ErrorMsg(
                task.getGenerateId(),
                task.getModuleName(),
                task.getState(),
                task.getTotalSize(),
                task.getFinishSize(),
                task.getRetryTime(),
                task.getErrorCode(),
                task.getException());
    }

}
