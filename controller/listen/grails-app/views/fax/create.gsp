<html>
  <head>
    <title><g:message code="page.fax.create.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="messages"/>
    <meta name="button" content="sendfax"/>
    <style type="text/css">
ol.file-list {
    list-style-type: none;
    margin: 0 10px 0 0;
    padding: 0;
    width: 510px;
}

ol.file-list > li {
    background-color: #E4F0FB;
    border: 1px dashed #6DACD6;
    cursor: move;
    display: block;
    height: 50px;
    margin: 5px 0;
    overflow: hidden;
    padding: 5px;
    width: 500px;
}

ol.file-list > li input {
    background-color: #FFFFFF;
    cursor: pointer;
    display: inline-block;
    overflow: hidden;
    padding: 2px;
    vertical-align: top;
    width: 400px;
}

ol.file-list > li button {
    float: right;
}

.files-label {
    margin-top: 10px;
}

#add-file {
    left: 410px;
    position: relative;
}

.sortable > li.placeholder {
    background-color: #6DACD6;
    border-color: #6DACD6;
}

.file-list,
fieldset .info-snippet {
    float: left;
}

fieldset .info-snippet {
    width: 300px;
}

fieldset ul.form-buttons {
    clear: both;
    float: left;
}

.templates {
    display: none;
}

ol.file-list > li.upload-error {
    border-color: #A81818;
    background-color: #FFC7C7;
}

div.upload-error {
    clear: both;
    color: #A81818;
    display: block;
    font-size: 14px;
    font-weight: bold;
}
    </style>
  </head>
  <body>
    <g:uploadForm name="file-form" controller="fax" action="save" method="post">
      <fieldset class="vertical">
        <label for="dnis"><g:message code="outgoingFax.dnis.label"/></label>
        <g:textField name="dnis" value="${fieldValue(bean: fax, field: 'dnis')}" class="${listen.validationClass(bean: fax, field: 'dnis')}" autocomplete="off"/>
        <listen:autocomplete selector="#dnis" data="all.direct"/>
        
        <div class="files-label">Files<button type="button" id="add-file">Add File</button></div>

        <ol class="file-list sortable">
          <g:if test="${fax?.sourceFiles?.size() > 0}">
            <g:set var="disableDelete" value="${fax.sourceFiles.size() == 1 && fax.sourceFiles[0].detectedType == 'application/pdf'}"/>
            <g:each in="${fax.sourceFiles}" var="file" status="i">
              <li class="${file.detectedType != 'application/pdf' ? 'upload-error' : ''}">
                <span class="file-existing">${file.file.name.encodeAsHTML()}</span>
                <input type="hidden" class="file-input" name="files[${i}]" value="${file.id}"/>
                <button type="button" class="delete-file${disableDelete ? ' disabled' : ''}"${disableDelete ? ' disabled="disabled" readonly="readonly"' : ''}>Delete</button>
                <g:if test="${file.detectedType != 'application/pdf'}">
                  <div class="upload-error">This file is not a PDF</div>
                </g:if>
              </li>
            </g:each>
          </g:if>
          <g:else>
            <li class="${hasErrors(bean: fax, field: 'sourceFiles', 'upload-error')}">
              <input type="file" class="file-input" name="files[0]"/>
              <button type="button" class="delete-file disabled" readonly="readonly" disabled="disabled">Delete</button>
            </li>
          </g:else>
        </ol>

        <listen:infoSnippet summaryCode="page.fax.create.snippet.summary" contentCode="page.fax.create.snippet.content"/>

        <ul class="form-buttons">
          <g:submitButton name="create" value="Send"/>
        </ul>
      </fieldset>
    </g:uploadForm>
    <ul class="templates">
      <li id="file-template"><input type="file" class="file-input"/><button type="button" class="delete-file">Delete</button></li>
    </ul>
    <script type="text/javascript">
var fax = {
    renumberFileList: function() {
        $('.file-list > li .file-input').each(function(index) {
            $(this).attr('name', 'files[' + index + ']');
        });
    },

    deleteFile: function(li) {
        li.remove();
        if($('.file-list > li').size() == 1 && !$('.file-list > li:first').hasClass('upload-error')) {
            $('.file-list > li .delete-file').addClass('disabled').attr('readonly', 'readonly').attr('disabled', 'disabled');
        }
    },

    addFile: function() {
        var clone = $('#file-template').clone(true).removeAttr('id');
        $('.file-list').append(clone);
        $('.file-list > li .delete-file').removeClass('disabled').removeAttr('readonly').removeAttr('disabled');
    }
};

$(document).ready(function() {
    $('#destination').focus();
    $('#add-file').click(function() {
        fax.addFile();
    });
    $('.sortable').sortable({
        opacity: .5,
        placeholder: 'placeholder',
        axis: 'y',
        tolerance: 'pointer'
    }).disableSelection();
    $('.delete-file').click(function(e) {
        if($('.file-list > li').size() == 1) {
            fax.addFile();
        }
        fax.deleteFile($(e.target).closest('li'));
    });
    $('#file-form').submit(function() {
        fax.renumberFileList();
        $('#create').addClass('disabled').attr('disabled', 'disabled').attr('readonly', 'readonly').val('Uploading...');
    });
});
    </script>
  </body>
</html>