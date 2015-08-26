/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2015 Aimluck,Inc.
 * http://www.aipo.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aipo.social.opensocial.spi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.cayenne.access.DataContext;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.multipart.FormDataItem;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.aipo.container.protocol.AipoErrorCode;
import com.aipo.container.protocol.AipoProtocolException;
import com.aipo.orm.Database;
import com.aipo.orm.model.security.TurbineUser;
import com.aipo.orm.service.TurbineUserDbService;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.google.inject.Inject;

/**
 *
 */
public abstract class AbstractService {

  @Inject
  private TurbineUserDbService turbineUserDbService;

  private String orgId;

  private String viewerId;

  /** 新仕様：画像サムネイルのサイズ大（横幅） */
  public static final int DEF_LARGE_THUMBNAIL_WIDTH = 200;

  /** 新仕様：画像サムネイルのサイズ（横幅） */
  public static final int DEF_NORMAL_THUMBNAIL_WIDTH = 100;

  /** 新仕様：画像サムネイルのサイズ大（縦幅） */
  public static final int DEF_LARGE_THUMBNAIL_HEIGHT = 200;

  /** 新仕様：画像サムネイルのサイズ（縦幅） */
  public static final int DEF_NORMAL_THUMBNAIL_HEIGHT = 100;

  /** 旧仕様：画像サムネイルのサイズ（横幅） */
  public static final int DEF_THUMBNAIL_WIDTH = 86;

  /** 旧仕様：画像サムネイルのサイズ（縦幅） */
  public static final int DEF_THUMBNAIL_HEIGHT = 86;

  /** 旧仕様：スマートフォンの画像サムネイルのサイズ（横幅） */
  public static final int DEF_THUMBNAIL_WIDTH_SMARTPHONE = 64;

  /** 旧仕様：スマートフォンの画像サムネイルのサイズ（縦幅） */
  public static final int DEF_THUMBNAIL_HEIGHT_SMARTPHONE = 64;

  /** 最小サムネイルサイズ（横幅） */
  public static final int DEF_VALIDATE_WIDTH = 200;

  /** 最小サムネイルサイズ（縦幅） */
  public static final int DEF_VALIDATE_HEIGHT = 200;

  protected void setUp(SecurityToken token) {

    try {
      String viewer = token.getViewerId();
      String[] split = viewer.split(":");

      if (split.length != 2) {
        throw new AipoProtocolException(
          AipoErrorCode.VALIDATE_ACCESS_NOT_DENIED);
      }

      orgId = split[0];
      viewerId = split[1];

      selectDataDomain(orgId);
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_NOT_DENIED);
    }

    checkViewerExists(token);

  }

  protected void selectDataDomain(String orgId) throws Exception {
    String currentOrgId = Database.getDomainName();
    if (currentOrgId == null) {
      DataContext dataContext = Database.createDataContext(orgId);
      DataContext.bindThreadObjectContext(dataContext);
    } else if (currentOrgId.equals(orgId)) {
      return;
    } else {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_NOT_DENIED);
    }
  }

  protected String getOrgId(SecurityToken token) {
    if (orgId == null) {
      setUp(token);
    }
    return orgId;
  }

  protected String getViewerId(SecurityToken token) {
    if (viewerId == null) {
      setUp(token);
    }
    return viewerId;
  }

  protected String convertUserId(String username, SecurityToken token) {
    return new StringBuilder(getOrgId(token))
      .append(":")
      .append(username)
      .toString();
  }

  protected String getUserId(String userId, SecurityToken token) {
    String[] split = userId.split(":");

    if (split.length != 2) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_NOT_DENIED);
    }

    String currentOrgId = split[0];
    String currentUserId = split[1];

    checkSameOrgId(currentOrgId, token);

    return currentUserId;

  }

  protected String getUserId(UserId userId, SecurityToken token) {
    return getUserId(userId.getUserId(token), token);
  }

  /**
   * 指定したデータベース名が、現在選択しているデータベース名と一致しているかチェックします。
   *
   * @param orgId
   * @param token
   */
  protected void checkSameOrgId(String orgId, SecurityToken token) {
    if ((orgId != null) && !("".equals(orgId))) {
      if (orgId.equals(getOrgId(token))) {
        return;
      }
    }
    throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_NOT_DENIED);
  }

  /**
   * Viewer が存在するかどうかチェックします。
   *
   * @param token
   * @throws ProtocolException
   */
  protected void checkViewerExists(SecurityToken token)
      throws ProtocolException {
    String viewerId = getViewerId(token);
    boolean result = false;
    try {
      TurbineUser user = turbineUserDbService.findByUsername(viewerId);
      result = user != null;
    } catch (Throwable t) {
      result = false;
    }
    if (!result) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_NOT_DENIED);
    }
  }

  /**
   * 指定されたユーザーが Viewer と一致しているかチェックします。
   *
   * @param userId
   * @param token
   * @throws ProtocolException
   */
  protected void checkSameViewer(UserId userId, SecurityToken token)
      throws ProtocolException {
    if (!getViewerId(token).equals(getUserId(userId, token))) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_NOT_DENIED);
    }
  }

  /**
   * 指定されたアプリが現在利用しているアプリと一致しているかチェックします。
   *
   * @param appId
   * @param token
   */
  protected void checkSameAppId(String appId, SecurityToken token) {
    if (appId != null && !appId.equals("")) {
      if (appId.equals(token.getAppId())) {
        return;
      }
    }
    throw new AipoProtocolException(AipoErrorCode.VALIDATE_ACCESS_NOT_DENIED);
  }

  protected void checkInputRange(String input, int min, int max) {
    if (input == null || (input.length() < min) || (input.length() > max)) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR);
    }
  }

  protected void checkInputByte(String input, int min, int max) {
    if (input == null || (byteLength(input) < min) || (byteLength(input) > max)) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_ERROR);
    }
  }

  private int byteLength(String value) {
    int len = 0;
    if (value == null) {
      return len;
    }

    try {
      len = (value.getBytes("utf-8")).length;
    } catch (UnsupportedEncodingException ex) {
      len = 0;
    }

    return len;
  }

  protected ShrinkImageSet getBytesShrink(FormDataItem formDataItem, int width,
      int height, boolean isFixOrgImage, int minWidth, int maxHeight) {

    byte[] result = null;
    byte[] fixResult = null;
    boolean fixed = false;

    try {

      String fileName = formDataItem.getName();
      String ext = null;
      String[] acceptExts = ImageIO.getWriterFormatNames();

      if (acceptExts != null && acceptExts.length > 0) {
        String tmpExt = null;
        for (String acceptExt : acceptExts) {
          if (!acceptExt.startsWith(".")) {
            tmpExt = "." + acceptExt;
          }
          if (fileName.toLowerCase().endsWith(tmpExt)) {
            ext = tmpExt.replace(".", "");
            break;
          }
        }
      }
      if (ext == null) {
        throw new AipoProtocolException(AipoErrorCode.VALIDATE_IMAGE_FORMAT);
      }

      byte[] imageInBytes = formDataItem.get();

      BufferedImage bufferdImage =
        ImageIO.read(new ByteArrayInputStream(imageInBytes));
      ImageInformation readImageInformation =
        readImageInformation(new ByteArrayInputStream(imageInBytes));
      if (readImageInformation != null) {
        bufferdImage =
          transformImage(
            bufferdImage,
            getExifTransformation(readImageInformation),
            readImageInformation.orientation >= 5
              ? bufferdImage.getHeight()
              : bufferdImage.getWidth(),
            readImageInformation.orientation >= 5
              ? bufferdImage.getWidth()
              : bufferdImage.getHeight());
        fixed = isFixOrgImage;
      }
      if (bufferdImage == null) {
        throw new AipoProtocolException(AipoErrorCode.VALIDATE_IMAGE_FORMAT);
      }

      BufferedImage shrinkImage =
        shrinkAndTrimImage(bufferdImage, width, height, minWidth, maxHeight);
      Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("jpeg");
      ImageWriter writer = writers.next();

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ImageOutputStream ios = ImageIO.createImageOutputStream(out);
      writer.setOutput(ios);
      writer.write(shrinkImage);

      result = out.toByteArray();

      if (fixed) {
        Iterator<ImageWriter> writers2 = ImageIO.getImageWritersBySuffix(ext);
        ImageWriter writer2 = writers2.next();

        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        ImageOutputStream ios2 = ImageIO.createImageOutputStream(out2);
        writer2.setOutput(ios2);
        writer2.write(bufferdImage);

        fixResult = out2.toByteArray();
      }
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      throw new AipoProtocolException(AipoErrorCode.VALIDATE_IMAGE_FORMAT);
    }

    return new ShrinkImageSet(result, fixed ? fixResult : null);
  }

  private BufferedImage shrinkAndTrimImage(BufferedImage imgfile, int width,
      int height, int validate_width, int validate_height) {
    int iwidth = imgfile.getWidth();
    int iheight = imgfile.getHeight();
    if (validate_width > 0 && validate_height > 0) {
      if (iwidth < validate_width || iheight < validate_height) {
        throw new AipoProtocolException(AipoErrorCode.VALIDATE_IMAGE_SIZE_200);
      }
    }

    double ratio =
      Math.max((double) width / (double) iwidth, (double) height
        / (double) iheight);

    int shrinkedWidth;
    int shrinkedHeight;

    if ((iwidth <= width) || (iheight < height)) {
      shrinkedWidth = iwidth;
      shrinkedHeight = iheight;
    } else {
      shrinkedWidth = (int) (iwidth * ratio);
      shrinkedHeight = (int) (iheight * ratio);
    }

    // イメージデータを縮小する
    Image targetImage =
      imgfile.getScaledInstance(
        shrinkedWidth,
        shrinkedHeight,
        Image.SCALE_AREA_AVERAGING);

    int w_size = targetImage.getWidth(null);
    int h_size = targetImage.getHeight(null);
    if (targetImage.getWidth(null) < width) {
      w_size = width;
    }
    if (targetImage.getHeight(null) < height) {
      h_size = height;
    }
    BufferedImage tmpImage =
      new BufferedImage(w_size, h_size, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = tmpImage.createGraphics();
    g.setBackground(Color.WHITE);
    g.setColor(Color.WHITE);
    // 画像が小さい時には余白を追加してセンタリングした画像にする
    g.fillRect(0, 0, w_size, h_size);
    int diff_w = 0;
    int diff_h = 0;
    if (width > shrinkedWidth) {
      diff_w = (width - shrinkedWidth) / 2;
    }
    if (height > shrinkedHeight) {
      diff_h = (height - shrinkedHeight) / 2;
    }
    g.drawImage(targetImage, diff_w, diff_h, null);

    int _iwidth = tmpImage.getWidth();
    int _iheight = tmpImage.getHeight();
    BufferedImage _tmpImage;
    if (_iwidth > _iheight) {
      int diff = _iwidth - width;
      _tmpImage = tmpImage.getSubimage(diff / 2, 0, width, height);
    } else {
      int diff = _iheight - height;
      _tmpImage = tmpImage.getSubimage(0, diff / 2, width, height);
    }
    return _tmpImage;
  }

  private ImageInformation readImageInformation(InputStream in) {
    try {
      Metadata metadata =
        ImageMetadataReader.readMetadata(new BufferedInputStream(in), true);
      Directory directory = metadata.getDirectory(ExifIFD0Directory.class);
      JpegDirectory jpegDirectory = metadata.getDirectory(JpegDirectory.class);

      int orientation = 1;
      try {
        orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
      } catch (Exception me) {

      }
      int width = jpegDirectory.getImageWidth();
      int height = jpegDirectory.getImageHeight();

      return new ImageInformation(orientation, width, height);
    } catch (ProtocolException e) {
      throw e;
    } catch (Throwable t) {
      // ignore
    }
    // JPEG 以外
    return null;
  }

  public static AffineTransform getExifTransformation(ImageInformation info) {

    AffineTransform t = new AffineTransform();
    if (info == null) {
      return t;
    }

    switch (info.orientation) {
      case 1:
        break;
      case 2: // Flip X
        t.scale(-1.0, 1.0);
        t.translate(-info.width, 0);
        break;
      case 3: // PI rotation
        t.translate(info.width, info.height);
        t.rotate(Math.PI);
        break;
      case 4: // Flip Y
        t.scale(1.0, -1.0);
        t.translate(0, -info.height);
        break;
      case 5: // - PI/2 and Flip X
        t.rotate(-Math.PI / 2);
        t.scale(-1.0, 1.0);
        break;
      case 6: // -PI/2 and -width
        t.translate(info.height, 0);
        t.rotate(Math.PI / 2);
        break;
      case 7: // PI/2 and Flip
        t.scale(-1.0, 1.0);
        t.translate(-info.height, 0);
        t.translate(0, info.width);
        t.rotate(3 * Math.PI / 2);
        break;
      case 8: // PI / 2
        t.translate(0, info.width);
        t.rotate(3 * Math.PI / 2);
        break;
      default:
        break;
    }

    return t;
  }

  private BufferedImage transformImage(BufferedImage image,
      AffineTransform transform, int newWidth, int newHeight) throws Exception {

    AffineTransformOp op =
      new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);

    BufferedImage destinationImage =
      new BufferedImage(newWidth, newHeight, image.getType());
    Graphics2D g = destinationImage.createGraphics();
    g.setColor(Color.WHITE);

    destinationImage = op.filter(image, destinationImage);

    return destinationImage;
  }

  public class ImageInformation {

    public final int orientation;

    public final int width;

    public final int height;

    public ImageInformation(int orientation, int width, int height) {
      this.orientation = orientation;
      this.width = width;
      this.height = height;
    }

    @Override
    public String toString() {
      return String.format(
        "%dx%d,%d",
        this.width,
        this.height,
        this.orientation);
    }
  }

  public class ShrinkImageSet {

    private byte[] shrinkImage = null;

    private byte[] fixImage = null;

    public ShrinkImageSet(byte[] shrinkImage, byte[] fixImage) {
      this.shrinkImage = shrinkImage;
      this.fixImage = fixImage;
    }

    public byte[] getShrinkImage() {
      return this.shrinkImage;
    }

    public byte[] getFixImage() {
      return this.fixImage;
    }
  }
}
